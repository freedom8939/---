package com.example.hospital.api.socket;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.support.json.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint(value = "/socket")
@Component
public class WebSocketService {
    public static ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {

    }

    @OnClose
    public void onClose(Session session) {
        Map map = session.getUserProperties();
        if (map.containsKey("userId")) {
            String userId = MapUtil.getStr(map, "userId");
            sessionMap.remove(userId);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        JSONObject json = JSONUtil.parseObj(message);
        String opt =json.getStr("opt");
        String token = json.getStr("token");
        Object loginIdByToken = StpUtil.stpLogic.getLoginIdByToken(token);

        String userId = StpUtil.stpLogic.getLoginIdByToken(token).toString();
        Map map = session.getUserProperties();

        if (!map.containsKey("userId")) {
            map.put("userId", userId);
        }

        if (sessionMap.containsKey(userId)) {
            sessionMap.replace(userId, session);
        } else {
            sessionMap.put(userId, session);
        }
        if ("ping".equals(opt)) {
            return;
        }

        sendInfo("ok", userId);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误", error);
    }

    public static void sendInfo(String message, String userId) {
        if (StrUtil.isNotBlank(userId) && sessionMap.containsKey(userId)) {
            Session session = sessionMap.get(userId);
            sendMessage(message, session);
        }
    }

    private static void sendMessage(String message, Session session) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("执行异常", e);
        }
    }
}