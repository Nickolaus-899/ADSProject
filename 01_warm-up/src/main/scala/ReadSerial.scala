// ADS I Class Project
// Chisel Introduction
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 18/10/2022 by Tobias Jauch (@tojauch)

package readserial

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum


/** controller class */
class Controller extends Module{
  
  val io = IO(new Bundle {
    /* 
     * TODO: Define IO ports of a the component as stated in the documentation
     */
    val rxd    = Input(UInt(1.W))
    val cnt_s  = Input(UInt(1.W))
    val cnt_en = Output(UInt(1.W))
    val valid  = Output(UInt(1.W))
    })

  // internal variables
  /* 
   * TODO: Define internal variables (registers and/or wires), if needed
   */

  object State extends ChiselEnum {
    val IDLE, COUNT_EN, READY = Value
  }

  val state = RegInit(State.IDLE)
  // state machine
  /* 
   * TODO: Describe functionality if the controller as a state machine
   */
  when(reset.asBool) {
    state := State.IDLE
  } .otherwise {

    switch (state) {
      is(State.IDLE) {
        when(!io.rxd) {
          state := State.COUNT_EN
        }
      }
      is(State.COUNT_EN) {
        when(io.cnt_s === 1.U) {
          state := State.READY
        }
      }
      is(State.READY) {
        when(io.rxd === 1.U){
          state := State.IDLE
        } .otherwise {
          state := State.COUNT_EN
        }
      }
    }
  }
  
  io.valid := 0.U
  io.cnt_en := 0.U
  when(reset.asBool) {
    io.cnt_en := 0.U
  } .otherwise {
    switch(state){
      is(State.IDLE){
        io.cnt_en := !io.rxd
      }
      is(State.COUNT_EN){
        io.cnt_en := 1.U
      }
      is(State.READY){
        io.cnt_en := !io.rxd
        io.valid  := 1.U
      }
    }
  }

}


/** counter class */
class Counter extends Module{
  
  val io = IO(new Bundle {
    /* 
     * TODO: Define IO ports of a the component as stated in the documentation
     */
    val cnt_en = Input(UInt(1.W))
    val cnt_s  = Output(UInt(1.W))
    })

  // internal variables
  /* 
   * TODO: Define internal variables (registers and/or wires), if needed
   */
  val cnt = Reg(UInt(4.W))

  val state = RegInit(State.IDLE)

  object State extends ChiselEnum {
    val IDLE, COUNT = Value
  }


  // state machine
  /* 
   * TODO: Describe functionality if the counter as a state machine
   */
  io.cnt_s := 0.U
  when(reset.asBool) {
    state := State.IDLE
    cnt   := 0.U
  } .otherwise {
    io.cnt_s := 0.U
    switch (state) {
      is(State.IDLE) {
        cnt   := 0.U
        when(io.cnt_en === 1.U) {
          state := State.COUNT
        }
      }
      is(State.COUNT) {
        cnt := cnt + 1.U
        when(cnt === 7.U) {
          state := State.IDLE
          io.cnt_s := 1.U
        }
      }
    }
  }
  
  

}

/** shift register class */
class ShiftRegister extends Module{
  
  val io = IO(new Bundle {
    /* 
     * TODO: Define IO ports of a the component as stated in the documentation
     */
    val rxd  = Input(UInt(1.W))
    val data = Output(UInt(8.W))
  })

  // internal variables
  val shift_reg = Reg(UInt(8.W))

  when(reset.asBool){
    shift_reg := 0.U
  } .otherwise {
    shift_reg := Cat(
      shift_reg(6, 0),
      io.rxd
    )
  }

  io.data := shift_reg
  /* 
   * TODO: Define internal variables (registers and/or wires), if needed
   */

  // functionality
  /* 
   * TODO: Describe functionality if the shift register
   */
}

/** 
  * The last warm-up task deals with a more complex component. Your goal is to design a serial receiver.
  * It scans an input line (“serial bus”) named rxd for serial transmissions of data bytes. A transmission 
  * begins with a start bit ‘0’ followed by 8 data bits. The most significant bit (MSB) is transmitted first. 
  * There is no parity bit and no stop bit. After the last data bit has been transferred a new transmission 
  * (beginning with a start bit, ‘0’) may immediately follow. If there is no new transmission the bus line 
  * goes high (‘1’, this is considered the “idle” bus signal). In this case the receiver waits until the next 
  * transmission begins. The outputs of the design are an 8-bit parallel data signal and a valid signal. 
  * The valid signal goes high (‘1’) for one clock cycle after the last serial bit has been transmitted, 
  * indicating that a new data byte is ready.
  */
class ReadSerial extends Module{
  
  val io = IO(new Bundle {
    /* 
     * TODO: Define IO ports of a the component as stated in the documentation
     */
    val rxd   = Input(UInt(1.W))
    val valid = Output(UInt(1.W))
    val data  = Output(UInt(8.W)) 
  })


  // instanciation of modules
  val controller0    = Module(new Controller)
  val counter0       = Module(new Counter)
  val shiftRegister0 = Module(new ShiftRegister)
  /* 
   * TODO: Instanciate the modules that you need
   */

  // connections between modules
  /* 
   * TODO: connect the signals between the modules
   */
  
  controller0.io.rxd := io.rxd
  controller0.io.cnt_s := counter0.io.cnt_s
  
  counter0.io.cnt_en := controller0.io.cnt_en

  shiftRegister0.io.rxd := io.rxd

  io.valid := controller0.io.valid
  io.data  := shiftRegister0.io.data 
  

  // global I/O 
  /* 
   * TODO: Describe output behaviour based on the input values and the internal signals
   */

}
