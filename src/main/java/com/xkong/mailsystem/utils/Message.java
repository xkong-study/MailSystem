package com.xkong.mailsystem.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private String from;
    private String to;
    private String content;
    private LocalDateTime time;
}