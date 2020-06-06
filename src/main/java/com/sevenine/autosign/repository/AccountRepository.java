package com.sevenine.autosign.repository;

import com.sevenine.autosign.domain.Account;
import com.sevenine.autosign.utils.Sign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;

@Component
public class AccountRepository {

    Logger logger = LoggerFactory.getLogger(Sign.class);

    public LinkedHashMap<String, Account> getAccountReadXml(String filePath){
        LinkedHashMap<String,Account> AccountMap = new LinkedHashMap<String,Account>();
        try{
            // 获得DocumentBuilder对象
            DocumentBuilder builder = this.getDocumentBuilder();

            // 通过xml文件输入流获取xml的document对象`
//            ClassPathResource classPathResource = new ClassPathResource("static/AccountList.xml");
//            InputStream inputStream =classPathResource.getInputStream();
            InputStream inputStream = new FileInputStream(new File(filePath));
            Document document = builder.parse(inputStream);
            //获取所有Account节点
            NodeList Accounts = document.getElementsByTagName("account");
            if(Accounts.getLength() == 0){
                logger.error("配置文件无账户信息");
                throw new Exception("配置文件无账户信息");
            }
            //遍历所有Account节点
            for ( int i = 0 ; i < Accounts.getLength() ; i ++) {
                //创建一个Account类
                Account acc = new Account();
                //获取当前Account节点
                Node Account = Accounts.item(i);
                //获取当前Account节点的所有子节点集合
                NodeList childNodes = Account.getChildNodes();
                //遍历Account节点的所有子节点(这里回车也算一个节点，所以遍历的效率略低)
                for(int j = 0 ; j < childNodes.getLength() ; j++ ){
                    //获得单个子节点
                    Node cNode = childNodes.item(j);
                    //判断若子节点名称为username，则取出其中内容
                    if ( cNode.getNodeName() == "username"){
                        String content = cNode.getFirstChild().getTextContent().trim();
                        acc.setUserName(content);
                    }
                    //判断若子节点名称为password，则取出其中内容
                    if ( cNode.getNodeName() == "password"){
                        String content = cNode.getFirstChild().getTextContent().trim();
                        acc.setPassword(content);
                    }
                    //判断若子节点名称为isvalid，则取出其中内容
                    if ( cNode.getNodeName() == "isvalid"){
                        String content = cNode.getFirstChild().getTextContent().trim();
                        acc.setIsValid(content);
                    }
                    //判断若子节点名称为realname，则取出其中内容
                    if ( cNode.getNodeName() == "realname"){
                        String content = cNode.getFirstChild().getTextContent().trim();
                        acc.setRealName(content);
                    }
                    //判断若子节点名称为lastsigntime，则取出其中内容
                    if ( "lastsigntime".equals(cNode.getNodeName()) ){
                        try {
                            String content = cNode.getFirstChild().getTextContent().trim();
                            acc.setLastSignTime(content);
                        }catch(NullPointerException e){
                            acc.setLastSignTime(null);
                        }
                    }
                }
                //将用户添加到Accountmap中
                AccountMap.put(String.valueOf(i),acc);
            }
            builder = null;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return AccountMap;
    }

    /**
     * 获得DocumentBuilder对象
     */
    public DocumentBuilder getDocumentBuilder(){
        // 获得DocumentBuilderFactory对象
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db =null;
        try {
            db= factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return db;
    }
}
