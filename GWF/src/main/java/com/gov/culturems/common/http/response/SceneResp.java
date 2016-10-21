package com.gov.culturems.common.http.response;

import com.gov.culturems.entities.TeaFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 6/22/15.
 */
public class SceneResp {

    private String SceneId;
    private String ParentSceneId;
    private String SceneName;
    private String SceneRemark;
    private String SceneLocation;
    private String ParentSceneName;
    private String AlertStatus;
    private String DeviceStatus;
    private String DeviceCount;
    private List<DeviceInfo> Devices;
    private String SubSceneCount;
    private int Status;
    private String AlertControl;

    private class DeviceInfo {
        private String DeviceId;
        private String DeviceName;
    }

    public TeaFactory convertToFactory() {
        TeaFactory teaFactory = new TeaFactory();
        teaFactory.setId(SceneId);
        teaFactory.setName(SceneName);
        return teaFactory;
    }

    public static List<TeaFactory> convertToFactoryList(List<SceneResp> sceneResps) {
        List<TeaFactory> factories = new ArrayList<>();
        for (SceneResp temp : sceneResps) {
            factories.add(temp.convertToFactory());
        }
        return factories;
    }

}
