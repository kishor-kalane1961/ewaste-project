package com.example.ewaste.mapper;

import com.example.ewaste.dto.PickupRequestDto;
import com.example.ewaste.model.PickupRequest;

public class PickupRequestMapper {

    public static PickupRequestDto toDto(PickupRequest entity) {
        if (entity == null) {
            return null;
        }
        PickupRequestDto dto = new PickupRequestDto();
        dto.setMobileNo(entity.getMobileNo());
        dto.setPickupAddress(entity.getPickupAddress());
        dto.setModelName(entity.getModel());      // mapping entity "model" → dto "modelName"
        dto.setModelNumber(null);                 // entity doesn’t have modelNumber → set null (or map later if added)
        dto.setPickupDate(entity.getPickupDate());
        dto.setPickupTime(entity.getPickupTime());
        return dto;
    }
}
