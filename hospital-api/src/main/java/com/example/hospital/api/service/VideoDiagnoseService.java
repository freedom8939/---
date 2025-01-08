package com.example.hospital.api.service;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Shiqi
 */
public interface VideoDiagnoseService {

    public void online(int userId);

    public boolean offline(int userId);

    public void updateOpenFlag(int userId, boolean open,String roomId);

    public ArrayList<HashMap> searchImageByVideoDiagnosedId(int videoDiagnoseId);


    public ArrayList<HashMap> searchOnlineDoctorList(String subName, String job);

    public HashMap searchVideoDiagnoseInfo(int userId);

    public HashMap refreshInfo(int userId);

}

