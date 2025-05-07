package ethernet_pcs

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import PCSCodes._

class EthernetPCSRXFSMTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "EthernetPCSRXFSM"

  // pick one representative symbol from each code group
  val idleSym    = IDLE.head
  val ssd1Sym    = SSD1.head
  val ssd2Sym    = SSD2.head
  val dataSym    = DATA.head
  val csResetSym = CSRESET.head
  val xmtErrSym  = XMT_ERR.head

  // for BAD_SSD we just need something that is not SSD2
  val badSym     = idleSym
  // for PREMATURE_END in RECEIVE, pick any valid that is not DATA, not CSRESET, not XMT_ERR
  val otherSym   = ssd2Sym

  // helper to poke one cycle of tx_symb_vector
  private def pokeSym(c: EthernetPCSTXFSM, sym: Vec[SInt]) = {
    c.io.rx_symb_vector.valid.poke(true.B)
    c.io.rx_symb_vector.bits.poke(sym)
    c.clock.step(1)
  }

  it should "go through Idle -> SSD1 -> SSD2 -> Transmit Data -> CSReset 1 -> CSReset 2 -> ESD1 -> ESD2 -> Idle" in {
    test(new EthernetPCSTXFSM()) { c =>
      c.io.config.poke(false.B)
      c.io.pcs_reset.poke(false.B)

      // 1) IDLE
      c.io.tx_er.poke(false.B)
      c.io.tx_en.poke(true.B)
      c.io.tx_symb_vector.ready.poke(true.B)
      c.clock.step(1)

      // 2) SSD1
      c.io.tx_symb_vector.ready.poke(true.B)
      c.clock.step(1)

      // 3) SSD2
      c.io.tx_symb_vector.ready.poke(true.B)
      c.clock.step(1)

      // 4) Transmit Data
      c.io.tx_symb_vector.ready.poke(true.B)
      c.io.tx_symb_vector.en.poke(false.B)
      c.clock.step(1)

      // 5) CSReset 1
      c.io.tx_symb_vector.ready.poke(true.B)
      c.io.col.expect(false.B)
      c.clock.step(1)

      // 6) CSReset 2
      c.io.tx_symb_vector.ready.poke(true.B)
      c.io.col.expect(false.B)
      c.clock.step(1)

      // 7) ESD1
      c.io.tx_symb_vector.ready.poke(true.B)
      c.io.col.expect(false.B)
      c.clock.step(1)

      // 8) ESD2 
      c.io.tx_symb_vector.ready.poke(true.B)
      c.io.col.expect(false.B)
      c.clock.step(1)

      // 9) Idle
      c.io.tx_symb_vector.ready.poke(true.B)
      c.io.col.expect(false.B)
      c.clock.step(1)
    }
  }

/*
  it should "flag BAD_SSD when the second start-symbol is wrong" in {
    test(new EthernetPCSTXFSM()) { c =>
      c.io.config.poke(false.B)
      c.io.pcs_reset.poke(false.B)

      // first SSD1
      pokeSym(c, ssd1Sym)
      // second symbol is not SSD2 → BAD_SSD
      pokeSym(c, badSym)

      c.io.receive1000BT .expect(true.B)
      c.io.rxerror_status.expect(true.B)
      c.io.rx_er         .expect(true.B)
      c.io.rxd           .expect("h0E".U)
    }
  }

  it should "inject DATA_ERROR and then recover to RECEIVE" in {
    test(new EthernetPCSTXFSM()) { c =>
      c.io.config.poke(false.B)
      c.io.pcs_reset.poke(false.B)

      // get into RECEIVE
      pokeSym(c, ssd1Sym); pokeSym(c, ssd2Sym); pokeSym(c, ssd2Sym)

      // inject XMT_ERR
      pokeSym(c, xmtErrSym)
      c.io.receive1000BT.expect(true.B)
      c.io.rx_dv.expect(true.B)
      c.io.rx_er.expect(true.B)
      c.io.rxd.expect("h55".U)

      // next valid symbol → back to RECEIVE
      pokeSym(c, ssd2Sym)
      c.io.receive1000BT.expect(true.B)
      c.io.rx_dv.expect(true.B)
    }
  }

  it should "go through CS_RESET → ESD_IDLE → IDLE after RECEIVE" in {
    test(new EthernetPCSTXFSM()) { c =>
      c.io.config.poke(false.B)
      c.io.pcs_reset.poke(false.B)

      // get into RECEIVE
      pokeSym(c, ssd1Sym); pokeSym(c, ssd2Sym); pokeSym(c, ssd2Sym)

      // CS_RESET_1
      pokeSym(c, csResetSym)
      c.io.receive1000BT .expect(false.B)
      c.io.rx_dv.expect(false.B)
      c.io.rx_er.expect(false.B)
      c.io.rxd.expect(0.U)

      // CS_RESET_2
      pokeSym(c, csResetSym)
      c.io.receive1000BT.expect(false.B)
      c.io.rx_dv.expect(false.B)
      c.io.rx_er.expect(false.B)
      c.io.rxd.expect(0.U)

      // ESD_IDLE_1
      pokeSym(c, csResetSym)
      c.io.receive1000BT.expect(false.B)
      c.io.rx_dv.expect(false.B)
      c.io.rx_er.expect(false.B)
      c.io.rxd.expect(0.U)

      // finally see IDLE
      pokeSym(c, idleSym)
      c.io.receive1000BT.expect(false.B)
      c.io.rx_dv.expect(false.B)
      c.io.rx_er.expect(false.B)
    }
  }
  */
}
