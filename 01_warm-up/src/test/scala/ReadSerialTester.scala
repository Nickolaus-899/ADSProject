// ADS I Class Project
// Chisel Introduction
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 18/10/2022 by Tobias Jauch (@tojauch)

package readserial

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec


/**
  *read serial tester
  */
class ReadSerialTester extends AnyFlatSpec with ChiselScalatestTester {

  /** Sends one transmission: start bit '0' followed by 8 data bits, MSB first.
    * valid must stay low while the bits come in and go high (with the received
    * byte on data) right after the last data bit.
    */
  def sendByte(dut: ReadSerial, byte: Int): Unit = {
    val bits = 0 +: (7 to 0 by -1).map(i => (byte >> i) & 1)
    for ((bit, n) <- bits.zipWithIndex) {
      dut.io.rxd.poke(bit.U)
      dut.clock.step(1)
      if (n < bits.length - 1) dut.io.valid.expect(0.U)
    }
    dut.io.valid.expect(1.U)
    dut.io.data.expect(byte.U)
  }

  /** Keeps the line idle ('1') and checks that nothing is received. */
  def idle(dut: ReadSerial, cycles: Int): Unit = {
    dut.io.rxd.poke(1.U)
    for (_ <- 0 until cycles) {
      dut.clock.step(1)
      dut.io.valid.expect(0.U)
    }
  }

  behavior of "ReadSerial"

  it should "not signal valid on an idle line" in {
    test(new ReadSerial).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      idle(dut, 20)
    }
  }

  it should "receive a single byte and keep valid high for one cycle only" in {
    test(new ReadSerial).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      idle(dut, 3)
      sendByte(dut, 0xA5)
      idle(dut, 5)
    }
  }

  it should "receive back-to-back bytes" in {
    test(new ReadSerial).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      idle(dut, 3)
      // 0x00: data bits look like start bits, 0xFF: data bits look like an idle line
      for (byte <- Seq(0x00, 0xFF, 0x3C, 0x81)) {
        sendByte(dut, byte)
      }
      idle(dut, 5)
      for (byte <- Seq(0x0F, 0x51)) {
        sendByte(dut, byte)
      }
      idle(dut, 5)
    }
  }

  it should "abort an ongoing transmission on reset" in {
    test(new ReadSerial).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      idle(dut, 3)

      // start bit + 4 data bits, then reset
      for (bit <- Seq(0, 1, 0, 1, 1)) {
        dut.io.rxd.poke(bit.U)
        dut.clock.step(1)
        dut.io.valid.expect(0.U)
      }
      dut.reset.poke(true.B)
      dut.clock.step(1)
      dut.reset.poke(false.B)

      // the aborted transmission must not produce a byte
      idle(dut, 12)

      // receiver works normally afterwards
      sendByte(dut, 0x5A)
      idle(dut, 3)
    }
  }
}
