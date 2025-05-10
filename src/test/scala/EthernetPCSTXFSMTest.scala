// src/test/scala/ethernet_pcs/EthernetPCSTXFSMTest.scala
package ethernet_pcs

import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import PCSCodes._

class EthernetPCSTXFSMTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "EthernetPCSTXFSM"

  // Pick one representative symbol from each Seq
  private val idleSym    = IDLE(0)
  private val ssd1Sym    = SSD1(0)
  private val ssd2Sym    = SSD2(0)
  private val err1Sym    = SSD1(0)     // ssd1Err reuses SSD1(0)
  private val err2Sym    = SSD2(0)     // ssd2Err reuses SSD2(0)
  private val xmtErrSym  = XMT_ERR(0)
  private val esd1Sym    = ESD1(0)
  private val esd2Sym    = ESD2(0)
  private val csResetSym = CSRESET(0)

  // A dummy “encoded” vector for transmitData
  private val dataVec = VecInit(0.S(3.W), 1.S(3.W), -1.S(3.W), 2.S(3.W))

  // Helper to drive one cycle and consume the Decoupled output
  private def cycleAndConsume(c: EthernetPCSTXFSM): Unit = {
    c.io.tx_symb_vector.ready.poke(true.B)
    c.clock.step(1)
  }

  it should "run the normal data sequence through all states back to IDLE" in {
    test(new EthernetPCSTXFSM()) { c =>
      // Tie off
      c.io.config.poke(false.B)
      c.io.tx_symb_vector_encoded.poke(dataVec)

      // 0) IDLE
      c.io.tx_en.poke(false.B); c.io.tx_er.poke(false.B); c.io.receive1000BT.poke(false.B)
      cycleAndConsume(c)
      c.io.state_test.expect(0.U)
      c.io.tx_symb_vector.valid.expect(true.B); c.io.tx_symb_vector.bits.expect(idleSym); c.io.col.expect(false.B)

      // 1) SSD1
      c.io.tx_en.poke(true.B); c.io.tx_er.poke(false.B); c.io.receive1000BT.poke(true.B)
      cycleAndConsume(c)
      c.io.state_test.expect(1.U); c.io.tx_symb_vector.bits.expect(ssd1Sym); c.io.col.expect(true.B)

      // 2) SSD2
      c.io.tx_en.poke(true.B); c.io.tx_er.poke(false.B); c.io.receive1000BT.poke(false.B)
      cycleAndConsume(c)
      c.io.state_test.expect(2.U); c.io.tx_symb_vector.bits.expect(ssd2Sym); c.io.col.expect(false.B)

      // 3) transmitData
      c.io.tx_en.poke(true.B); c.io.tx_er.poke(false.B); c.io.receive1000BT.poke(true.B)
      cycleAndConsume(c)
      c.io.state_test.expect(6.U); c.io.tx_symb_vector.bits.expect(dataVec); c.io.col.expect(true.B)

      // 4) firstCsReset
      c.io.tx_en.poke(false.B); c.io.tx_er.poke(false.B)
      cycleAndConsume(c)
      c.io.state_test.expect(9.U); c.io.tx_symb_vector.bits.expect(csResetSym); c.io.col.expect(false.B)

      // 5) secondCsReset
      cycleAndConsume(c)
      c.io.state_test.expect(10.U); c.io.tx_symb_vector.bits.expect(csResetSym)

      // 6) esd1
      cycleAndConsume(c)
      c.io.state_test.expect(7.U); c.io.tx_symb_vector.bits.expect(esd1Sym)

      // 7) esd2
      cycleAndConsume(c)
      c.io.state_test.expect(8.U); c.io.tx_symb_vector.bits.expect(esd2Sym)

      // 8) back to IDLE
      cycleAndConsume(c)
      c.io.state_test.expect(0.U); c.io.tx_symb_vector.bits.expect(idleSym)
    }
  }

  it should "run the error sequence (tx_er path) through all states back to IDLE" in {
    test(new EthernetPCSTXFSM()) { c =>
      c.io.config.poke(false.B)
      c.io.tx_symb_vector_encoded.poke(dataVec)

      // 0) IDLE
      c.io.tx_en.poke(false.B); c.io.tx_er.poke(false.B); c.io.receive1000BT.poke(false.B)
      cycleAndConsume(c)
      c.io.state_test.expect(0.U)

      // 1) ssd1Err
      c.io.tx_en.poke(true.B); c.io.tx_er.poke(true.B); c.io.receive1000BT.poke(true.B)
      cycleAndConsume(c)
      c.io.state_test.expect(3.U); c.io.tx_symb_vector.bits.expect(err1Sym); c.io.col.expect(true.B)

      // 2) ssd2Err
      c.io.tx_en.poke(true.B); c.io.tx_er.poke(true.B)
      cycleAndConsume(c)
      c.io.state_test.expect(4.U); c.io.tx_symb_vector.bits.expect(err2Sym)

      // 3) transmitErr
      c.io.tx_en.poke(true.B); c.io.tx_er.poke(true.B); c.io.receive1000BT.poke(false.B)
      cycleAndConsume(c)
      c.io.state_test.expect(5.U); c.io.tx_symb_vector.bits.expect(xmtErrSym); c.io.col.expect(false.B)

      // 4) firstCsReset
      c.io.tx_en.poke(false.B); c.io.tx_er.poke(false.B)
      cycleAndConsume(c)
      c.io.state_test.expect(9.U)

      // 5) secondCsReset
      cycleAndConsume(c)
      c.io.state_test.expect(10.U)

      // 6) esd1
      cycleAndConsume(c)
      c.io.state_test.expect(7.U)

      // 7) esd2
      cycleAndConsume(c)
      c.io.state_test.expect(8.U)

      // 8) back to IDLE
      cycleAndConsume(c)
      c.io.state_test.expect(0.U)
    }
  }

  it should "stall the FSM when tx_symb_vector.ready is false" in {
    test(new EthernetPCSTXFSM()) { c =>
      c.io.config.poke(false.B)
      c.io.tx_symb_vector_encoded.poke(dataVec)

      // drive into ssd1
      c.io.tx_en.poke(true.B);  c.io.tx_er.poke(false.B); c.io.receive1000BT.poke(true.B)
      cycleAndConsume(c)
      c.io.state_test.expect(1.U)

      // back-pressure: ready = false
      c.io.tx_symb_vector.ready.poke(false.B)
      c.io.tx_en.poke(true.B);  c.io.tx_er.poke(false.B); c.io.receive1000BT.poke(false.B)
      c.clock.step(3)
      c.io.state_test.expect(1.U)

      // release back-pressure
      c.io.tx_symb_vector.ready.poke(true.B)
      c.clock.step(1)
      c.io.state_test.expect(2.U)
    }
  }

  it should "stay in ssd2 when tx_er is high but tx_en is deasserted" in {
    test(new EthernetPCSTXFSM()) { c =>
      c.io.config.poke(false.B)
      c.io.tx_symb_vector_encoded.poke(dataVec)

      // get into ssd2
      c.io.tx_en.poke(true.B);  c.io.tx_er.poke(false.B); c.io.receive1000BT.poke(true.B)
      cycleAndConsume(c)
      c.io.tx_en.poke(true.B);  c.io.tx_er.poke(false.B); c.io.receive1000BT.poke(false.B)
      cycleAndConsume(c)
      c.io.state_test.expect(2.U)

      // glitch: tx_en = false, tx_er = true
      c.io.tx_en.poke(false.B); c.io.tx_er.poke(true.B); c.io.receive1000BT.poke(false.B)
      cycleAndConsume(c)
      c.io.state_test.expect(2.U)
      c.io.tx_symb_vector.bits.expect(ssd2Sym)
    }
  }
}
