package com.donut.mixmessage.util.encode.encoders.bean;

import com.donut.mixmessage.kv


public interface TextCoder {


    val name: String


    fun encode(input: String, password: String = "123"): CoderResult
    fun decode(input: String, password: String = "123"): CoderResult

    fun generatePrefix(): String {
        return ""
    }


    fun checkText(input: String): Boolean {
        return input.isNotEmpty()
    }

    fun splitText(input: String): List<String> {

        return input.split(" ", "\n", "\t", "\r")
    }

    fun enable(): Unit {
        kv.encode("enable_encoder_$name", true)
    }

    fun disable(): Unit {
        kv.encode("enable_encoder_$name", false)
    }

    fun isEnabled(): Boolean {
        return kv.decodeBool("enable_encoder_$name", true)
    }


}
