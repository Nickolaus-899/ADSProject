// ADS I Class Project
// Chisel Introduction
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 18/10/2022 by Tobias Jauch (@tojauch)

package adder

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec


/** 
  * Full adder tester
  * Use the truth table from the exercise sheet to test all possible input combinations and the corresponding results exhaustively
  */
class FullAdderTester extends AnyFlatSpec with ChiselScalatestTester {

  "FullAdder" should "work" in {
    test(new FullAdder).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

          /*dut.io.a.poke(...)
           *dut.io.b.poke(...)
           *dut.io.ci.poke(...)
           *dut.io.s.expect(...)
           *dut.io.co.expect(...)
           *...
           *TODO: Insert your test cases
           */
          for(a <- 0 to 1){
            for(b <- 0 to 1){
              for(ci <- 0 to 1) {
                val result_s = a ^ b ^ ci
                val result_cout = (a & b) | (ci & (a ^ b))

                dut.io.a.poke(a.U)        // input a is set to value a 
                dut.io.b.poke(b.U)        // input b is set to value b
                dut.io.cin.poke(ci.U)        // input b is set to value b
                dut.clock.step(1)         // move one clock cycle ahead       
                dut.io.s.expect(result_s.U) // see if output c matches expected result
                dut.io.cout.expect(result_cout.U)
              }
           }
         }

        }
    } 
}

