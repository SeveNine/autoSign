package com.sevenine.autosign.utils;

import com.sevenine.autosign.config.Config;
import com.sevenine.autosign.domain.Account;
import com.sevenine.autosign.domain.MoveEntity;
import com.sevenine.autosign.repository.AccountRepository;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 考勤
 * created by xu-jp on 2020/5/23
 **/
@Component
public class Sign {
    Logger logger = LoggerFactory.getLogger(Sign.class);

    @Autowired
    private Config config;

    /**
     * 滑动验证码签到
     * created by xu-jp on 2020/5/27
     **/
    public void sign(LinkedHashMap<String, Account> accounts){
        // 实际打卡人数
        int record = 0;
        // 需要打卡人数
        int needRecord = 0;
        for (String key : accounts.keySet()){
            Account account = accounts.get(key);
            // 判断是否需要打卡
            if("false".equals(account.getIsValid())){
                logger.info("=={}==跳过打卡",account.getRealName());
            }else{
                needRecord++;
                // 打卡次数
                int counter = 0;
                // 打卡标志位
                boolean isSign = false;
                while(!isSign){
                    WebDriver driver = null;
                    try {
                        // 最多打三次卡，密码可能被修改，尝试次数过多会锁定账户
                        if (++counter > config.getReTryCount()) {
                            break;
                        }
                        //指定浏览器驱动路径
                        System.setProperty ( "webdriver.chrome.driver", config.getDriverPath() );
                        //初始化浏览器名为driver
                        driver = new ChromeDriver();
                        //窗口最大化
                        driver.manage ().window ().maximize ();
                        //使用get()方法，打开网址
                        driver.get (config.getTargetPath());
                        // 填写表单
                        this.fillForm(account,driver);
                        // 模拟滑动直至登录
                        while(!this.simulateSlider(driver)){};
                        // 签到
                        isSign = loginAndSign(driver);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        // 关闭浏览器
                        driver.quit();
                    }
                }
                if(isSign){
                    record++;
                    logger.info("=={}==打卡次数{}，成功",account.getRealName(),counter);
                }else{
                    logger.info("=={}==打卡次数{}，失败!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!",account.getRealName(),counter);
                }
            }
        }
        logger.info("==打卡成功{}人，失败{}人==",record,needRecord-record);

    }

    /**
     * 填写表单
     * created by xu-jp on 2020/5/27
     **/
    private void fillForm(Account account,WebDriver driver){
        //根据class选中表单，输入账户密码
        List<WebElement> we = driver.findElements(By.className("textfield"));
        we.get(0).sendKeys(account.getUserName());
        we.get(1).sendKeys(account.getPassword());
    }

    /**
     * 模拟滑动验证码
     * created by xu-jp on 2020/5/27
     **/
    private boolean simulateSlider(WebDriver driver) throws InterruptedException, IOException {
        WebElement element = null;
        Actions actions = null;
        int distance = 0;
        // 单击一下，让图片初始化/刷新
        element = driver.findElement(By.cssSelector(".init"));
        element.click();

        Thread.sleep(2 * 1000);
        // 用xpath定位验证码元素
        WebElement ele = driver.findElement(By.id("bigImage"));
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        // 获取当前页面屏幕截图
        BufferedImage fullImg = ImageIO.read(screenshot);
        // 获取元素的位置
        Point point = ele.getLocation();
        // 获取元素的宽和高
        int eleWidth = ele.getSize().getWidth();
        int eleHeight = ele.getSize().getHeight();
        // 将滑动验证码从屏幕截图中剪切出来
        BufferedImage eleScreenshot = fullImg.getSubimage(point.getX(), point.getY(), eleWidth, eleHeight);
        ImageIO.write(eleScreenshot, "png", screenshot);
//        System.out.println("文件在："+screenshot.toPath());
//        File screenshotLocation = new File("img\\sliderImg.png");
//        copyFile(screenshot,screenshotLocation);

        actions = new Actions(driver);
        // 计算需要滑动的距离
        distance = calcMoveDistance(eleWidth,screenshot);
        // 包装移动实体
        List<MoveEntity> list = getMoveEntity(distance);
        // 获取滑块元素
        element = driver.findElement(By.cssSelector(".init"));
        // 按住
        actions.clickAndHold(element).perform();
        // 滑动
        for (MoveEntity moveEntity : list) {
            actions.moveByOffset(moveEntity.getX(), moveEntity.getY()).perform();
            Thread.sleep(moveEntity.getSleepTime());
        }
//            Thread.sleep((long) (Math.random()*1000));
        // 松开
        actions.release(element).perform();
        Thread.sleep(2 * 1000);
        // 判断是否成功
        element = driver.findElement(By.cssSelector(".ui-slider-text"));
        if(element.getText().contains("验证成功")){
            return true;
        }
        return false;
    }

    /**
     * 登录并打卡
     * created by xu-jp on 2020/5/27
     **/
    private boolean loginAndSign(WebDriver driver){
        // 登录按钮
        WebElement element = driver.findElement(By.id("loginButton"));
        element.click();
        // 新增判断早上是否打卡过
        if (this.isMorning()){
            // 获取页面所有td，一行5个td，也可以使用tr判断是否有打卡记录
            List<WebElement> welist = driver.findElements(By.tagName("td"));
            // 若无打卡记录或者没登录成功则打卡（打卡代码会被catch）
            if(welist.size() < 5){
                driver.findElement(By.className("mr36")).click();
            }else{
                // 打过卡，无操作
            }
        }else{
            // 非早上正常打卡
            driver.findElement(By.className("mr36")).click();
        }
        return true;
    }

    /**
     * 移动算法
     */
    public static List<MoveEntity> getMoveEntity(int distance){
        List<MoveEntity> list = new ArrayList<>();
        for (int i = 0 ;i < distance / 5; i++){
            MoveEntity moveEntity = new MoveEntity();
            moveEntity.setX(5);
            moveEntity.setY(ThreadLocalRandom.current().nextBoolean() ? 10 : -10);
            moveEntity.setSleepTime(10);
            list.add(moveEntity);
        }

        MoveEntity moveEntity = new MoveEntity();
        moveEntity.setX(distance % 5);
        moveEntity.setY(0);
        moveEntity.setSleepTime(10);
        list.add(moveEntity);
        return list;
    }

    /**
     * 计算小方块需要移动的距离
     */
    public static int calcMoveDistance( float bgWrapWidth,File file) throws IOException {
        BufferedImage fullBI = ImageIO.read(file);
        int distance = 0;
        ArrayList<Integer> compareWidth = new ArrayList<Integer>();
        for(int w = 0 ,i = 0; w < fullBI.getWidth(); w++){
            int whiteLineLen = 0;
            for (int h = 0; h < fullBI.getHeight(); h++){
                int[] fullRgb = new int[3];
                fullRgb[0] = (fullBI.getRGB(w, h)  & 0xff0000) >> 16;
                fullRgb[1] = (fullBI.getRGB(w, h)  & 0xff00) >> 8;
                fullRgb[2] = (fullBI.getRGB(w, h)  & 0xff);
//                if (isBlack28(fullBI, w, h) && isWhite(fullBI, w, h)) {
                if (isWhite(fullBI, w, h)) {
                    whiteLineLen++;
                } else {
//                    whiteLineLen = 0;
                    continue;
                }
            }
            if(whiteLineLen >= 24){
//                System.out.println("检测到的缺口位置w="+w+"whiteLineLen="+whiteLineLen);
                compareWidth.add(i,w);
                i++;
            }

        }
//        System.out.println("需要移动的距离为："+distance);
        distance = compareWidth.get(2)+17+43;
        return distance;
    }

    /**
     * 判断当前点是不是白色
     */
    private static boolean isWhite(BufferedImage fullBI, int w, int h) {
        int[] fullRgb = new int[3];
//        System.out.println("rgb:"+fullBI.getRGB(w, h));
        fullRgb[0] = (fullBI.getRGB(w, h) & 0xff0000) >> 16;
        fullRgb[1] = (fullBI.getRGB(w, h) & 0xff00) >> 8;
        fullRgb[2] = (fullBI.getRGB(w, h) & 0xff);
//        System.out.println("fullRgb"+fullRgb[0]+"," +fullRgb[1]+"," +fullRgb[2]);

        return isWhite(fullRgb);
    }
    private static boolean isWhite(int[] fullRgb) {
        return fullRgb[0]>250 && fullRgb[1]>250 && fullRgb[2]>250;
    }

    /**
     * 判断是否为上午，大于12点
     * created by xu-jp on 2020/5/28
     **/
    private boolean isMorning(){
        boolean result = false;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd 12:00:00");
        // 获取当前时间和10.
        String nowtime = df.format(new Date());
        String nowtime2 = df2.format(new Date());
        try {
            //对比
            Date d1 = df.parse(nowtime);
            Date d2 = df.parse(nowtime2);
            // 对比
            if(d1.getTime() < d2.getTime()){
                result = true;
            }
        } catch (ParseException e) {
            logger.error("时间转换异常");
        }
        return result;

    }

    /**
     * 文件复制
     * created by xu-jp on 2020/6/6
     **/
//    public static void copyFile(File sourceFile,File targetFile)
//            throws IOException {
//
//        FileInputStream input = new FileInputStream(sourceFile);
//        BufferedInputStream inBuff=new BufferedInputStream(input);
//        FileOutputStream output = new FileOutputStream(targetFile);
//        BufferedOutputStream outBuff=new BufferedOutputStream(output);
//        byte[] b = new byte[1024 * 5];
//        int len;
//        while ((len =inBuff.read(b)) != -1) {
//            outBuff.write(b, 0, len);
//        }
//        outBuff.flush();
//        inBuff.close();
//        outBuff.close();
//        output.close();
//        input.close();
//    }



}
