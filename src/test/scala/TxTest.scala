package ethernet_pcs

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import PCSCodes._
import org.scalatest.matchers.should.Matchers
import scala.util.Random

class EthernetPCSTXFSMTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "EthernetPCSTXFSM"

  it should "go through Idle -> SSD1 -> SSD2 -> Transmit Data -> CSReset 1 -> CSReset 2 -> ESD1 -> ESD2 -> Idle" in {
    test(new EthernetPCSTXFSM()) { c =>
      c.io.config.poke(false.B)
      c.io.pcs_reset.poke(false.B)

      // 1) IDLE
      c.io.tx_er.poke(false.B)
      c.io.tx_en.poke(true.B)
      c.io.tx_symb_vector.ready.poke(true.B)
      c.io.state_test.expect(0.U(4.W))
      c.clock.step(1)

      // 2) SSD1
      c.io.tx_symb_vector.ready.poke(true.B)
      c.io.state_test.expect(1.U(4.W))
      c.clock.step(1)

      // 3) SSD2
      c.io.tx_symb_vector.ready.poke(true.B)
      c.io.state_test.expect(2.U(4.W))
      c.clock.step(1)

      // 4) Transmit Data
      c.io.tx_symb_vector.ready.poke(true.B)
      c.io.tx_en.poke(false.B)
      c.io.state_test.expect(6.U(4.W))
      c.clock.step(1)

      // 5) CSReset 1
      c.io.tx_symb_vector.ready.poke(true.B)
      c.io.col.expect(false.B)
      c.io.state_test.expect(9.U(4.W))
      c.clock.step(1)

      // 6) CSReset 2
      c.io.tx_symb_vector.ready.poke(true.B)
      c.io.col.expect(false.B)
      c.io.state_test.expect(10.U(4.W))
      c.clock.step(1)

      // 7) ESD1
      c.io.tx_symb_vector.ready.poke(true.B)
      c.io.col.expect(false.B)
      c.io.state_test.expect(7.U(4.W))
      c.clock.step(1)

      // 8) ESD2 
      c.io.tx_symb_vector.ready.poke(true.B)
      c.io.col.expect(false.B)
      c.io.state_test.expect(8.U(4.W))
      c.clock.step(1)

      // 9) Idle
      c.io.tx_symb_vector.ready.poke(true.B)
      c.io.col.expect(false.B)
      c.io.state_test.expect(0.U(4.W))
      c.clock.step(1)
    }
  }

  "EthernetPCSTXFSM" should "not deadlock when given random inputs" in {
    test(new EthernetPCSTXFSM()) { c =>
      val rnd = new scala.util.Random(1234) 
      for (_ <- 0 until 100) {
        c.io.config.poke(rnd.nextBoolean().B)
        c.io.pcs_reset.poke(rnd.nextBoolean().B)
        c.io.tx_er.poke(rnd.nextBoolean().B)
        c.io.tx_en.poke(rnd.nextBoolean().B)
        c.io.txd.poke(rnd.nextInt(256).U)
        c.io.receive1000BT.poke(rnd.nextBoolean().B)
        c.io.tx_symb_vector_encoded(0).poke((rnd.nextInt(5) - 2).S(3.W))
        c.io.tx_symb_vector_encoded(1).poke((rnd.nextInt(5) - 2).S(3.W))
        c.io.tx_symb_vector_encoded(2).poke((rnd.nextInt(5) - 2).S(3.W))
        c.io.tx_symb_vector_encoded(3).poke((rnd.nextInt(5) - 2).S(3.W))
        c.io.tx_symb_vector.ready.poke(true.B)

        val stateTest = c.io.state_test.peek()
        println(s"State Test Output is: {$stateTest}")

        c.clock.step(1)
      }
    }
  }

  "EthernetPCSTXFSM" should "run the error sequence (tx_er path) through all states back to IDLE" in {
    test(new EthernetPCSTXFSM()) { c =>
      c.io.tx_en.poke(true.B)
      c.io.tx_er.poke(true.B)
      c.io.tx_symb_vector.ready.poke(true.B)
      c.io.tx_er.poke(true.B)
      c.io.state_test.expect(0.U)
      c.clock.step(1)

      c.io.state_test.expect(3.U)
      c.clock.step(1)

      c.io.state_test.expect(4.U)
      c.clock.step(1)

      c.io.state_test.expect(5.U)
      for (_ <- 0 until 10) {
        c.clock.step(1)
        c.io.state_test.expect(5.U)
      }
      c.io.tx_er.poke(false.B)
      c.clock.step(1)

      c.io.state_test.expect(6.U)
      c.io.tx_en.poke(false.B)
      c.clock.step(1)

      c.io.state_test.expect(9)
      c.clock.step(1)

      c.io.state_test.expect(10)
      c.clock.step(1)

      c.io.state_test.expect(7)
      c.clock.step(1)

      c.io.state_test.expect(8)
      c.clock.step(1)

      c.io.state_test.expect(0)
    }
  }

  "EthernetPCSTXFSM" should "stall the FSM when PUDR is LOW" in {
    test(new EthernetPCSTXFSM()) { c =>
      c.io.tx_en.poke(true.B)
      c.io.tx_er.poke(false.B)
      c.io.tx_symb_vector.ready.poke(false.B)

      for (_ <- 0 until 10) {
        c.clock.step(1)
        c.io.state_test.expect(0.U)
      }
    }
  }

  "EthernetPCSTXFSM" should "stay in SSD2 when tx_er is high but tx_en is deasserted" in {
    test(new EthernetPCSTXFSM()) { c =>
      c.io.tx_en.poke(true.B)
      c.io.tx_er.poke(false.B)
      c.io.tx_symb_vector.ready.poke(true.B)

      c.clock.step(1)
      c.clock.step(1)
      c.io.tx_en.poke(false.B)
      c.io.tx_er.poke(true.B)
      c.io.state_test.expect(2.U)

      for (_ <- 0 until 5) {
        c.clock.step(1)
        c.io.state_test.expect(2.U)
      }
    }
  }
}