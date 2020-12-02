package ru.bloker.message;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Fix implements Serializable {

    private String message;
}
