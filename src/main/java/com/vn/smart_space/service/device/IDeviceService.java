package com.vn.smart_space.service.device;

import com.vn.smart_space.dto.request.device.RegisterFcmTokenRequest;

public interface IDeviceService {

    void registerFcmToken(String userId, RegisterFcmTokenRequest request);

    void clearFcmToken(String fcmToken);
}
