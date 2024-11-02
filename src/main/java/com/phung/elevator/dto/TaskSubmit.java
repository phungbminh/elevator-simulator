package com.phung.elevator.dto;

import com.phung.elevator.utils.EleState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskSubmit {
    private Integer floor;
    private EleState stage;
}
