package com.bank.cashdesk.dto;

import com.bank.cashdesk.model.Denomination;
import jakarta.validation.constraints.Min;

import java.util.List;
import java.util.stream.Collectors;

public record DenominationDTO(

        @Min(value = 1, message = "Denomination value must be at least 1")
        int value,

        @Min(value = 1, message = "Denomination quantity must be at least 1")
        int quantity

) {
    public static Denomination of(DenominationDTO dto) {
        return Denomination.builder().value(dto.value()).quantity(dto.quantity()).build();
    }

    public static List<Denomination> of(List<DenominationDTO> dtos) {
        return dtos.stream()
                .map(DenominationDTO::of)
                .collect(Collectors.toList());
    }

    public static DenominationDTO ofModel(Denomination denomination) {
        return new DenominationDTO(denomination.getValue(), denomination.getQuantity());
    }

    public static List<DenominationDTO> ofModel(List<Denomination> denominations) {
        return denominations.stream()
                .map(DenominationDTO::ofModel)
                .collect(Collectors.toList());
    }

}
