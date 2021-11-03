/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alkemy.java.exception;

import org.springframework.validation.BindingResult;


public class InvalidDataException extends RuntimeException{
    private static final long serialVersionUID = 1L;

  private final transient BindingResult result;

  public InvalidDataException(BindingResult result) {
    super();
    this.result = result;
  }

  public InvalidDataException(String message, BindingResult result) {
    super(message);
    this.result = result;
  }

  public BindingResult getResult() {
    return result;
  }
}
