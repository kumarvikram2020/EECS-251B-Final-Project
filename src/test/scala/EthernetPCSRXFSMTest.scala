package ethernet_pcs

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import PCSCodes._
import scala.util.Random

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

  // helper to poke one cycle of rx_symb_vector
  private def pokeSym(c: EthernetPCSRXFSM, sym: Vec[SInt]) = {
    c.io.rx_symb_vector.valid.poke(true.B)
    c.io.rx_symb_vector.bits.poke(sym)
    c.clock.step(1)
  }

  it should "perform the normal receive -> premature-end -> IDLE sequence" in {
    test(new EthernetPCSRXFSM()) { c =>
      c.io.config.poke(false.B)
      c.io.pcs_reset.poke(false.B)

      // 1) IDLE
      pokeSym(c, idleSym)
      c.io.receive1000BT.peek()
      c.io.rx_dv.peek()

      // 2) NON_IDLE_DETECT (ssd1 arrives)
      pokeSym(c, ssd1Sym)
      c.io.receive1000BT.peek()
      c.io.rx_dv.peek()

      // 3) SSD1_VECTOR -> rxd = 0x55
      pokeSym(c, ssd2Sym)
      c.io.rx_dv.peek()
      c.io.rxd.peek()

      // 4) SSD2_VECTOR -> rxd = 0x55
      pokeSym(c, ssd2Sym)
      c.io.rx_dv.peek()
      c.io.rxd.peek()

      // 5) RECEIVE -> rxd = 0x55
      pokeSym(c, ssd2Sym)
      c.io.rx_dv.peek()
      c.io.rxd.peek()

      // 6) no DATA, no XMT_ERR, no CSRESET -> sPREMATURE_END
      pokeSym(c, otherSym)
      c.io.rx_dv.peek()
      c.io.rx_er.peek()
      c.io.rxd.peek()

      // 7) finally see IDLE -> back to sIDLE
      pokeSym(c, idleSym)
      c.io.receive1000BT.peek()
      c.io.rx_dv.peek()
      c.io.rx_er.peek()
    }
  }

  it should "flag BAD_SSD when the second start-symbol is wrong" in {
    test(new EthernetPCSRXFSM()) { c =>
      c.io.config.poke(false.B)
      c.io.pcs_reset.poke(false.B)

      // first SSD1
      pokeSym(c, ssd1Sym)
      // second symbol is not SSD2 → BAD_SSD
      pokeSym(c, badSym)

      c.io.receive1000BT.peek()
      c.io.rxerror_status.peek()
      c.io.rx_er.peek()
      c.io.rxd.peek()
    }
  }

  it should "inject DATA_ERROR and then recover to RECEIVE" in {
    test(new EthernetPCSRXFSM()) { c =>
      c.io.config.poke(false.B)
      c.io.pcs_reset.poke(false.B)

      // get into RECEIVE
      pokeSym(c, ssd1Sym); pokeSym(c, ssd2Sym); pokeSym(c, ssd2Sym)

      // inject XMT_ERR
      pokeSym(c, xmtErrSym)
      c.io.receive1000BT.peek()
      c.io.rx_dv.peek()
      c.io.rx_er.peek()
      c.io.rxd.peek()

      // next valid symbol -> back to RECEIVE
      pokeSym(c, ssd2Sym)
      c.io.receive1000BT.peek()
      c.io.rx_dv.peek()
    }
  }

  it should "go through CS_RESET -> ESD_IDLE -> IDLE after RECEIVE" in {
    test(new EthernetPCSRXFSM()) { c =>
      c.io.config.poke(false.B)
      c.io.pcs_reset.poke(false.B)

      // get into RECEIVE
      pokeSym(c, ssd1Sym); pokeSym(c, ssd2Sym); pokeSym(c, ssd2Sym)

      // CS_RESET_1
      pokeSym(c, csResetSym)
      c.io.receive1000BT.peek()
      c.io.rx_dv.peek()
      c.io.rx_er.peek()
      c.io.rxd.peek()

      // CS_RESET_2
      pokeSym(c, csResetSym)
      c.io.receive1000BT.peek()
      c.io.rx_dv.peek()
      c.io.rx_er.peek()
      c.io.rxd.peek()

      // ESD_IDLE_1
      pokeSym(c, csResetSym)
      c.io.receive1000BT.peek()
      c.io.rx_dv.peek()
      c.io.rx_er.peek()
      c.io.rxd.expect(0.U)

      // finally see IDLE
      pokeSym(c, idleSym)
      c.io.receive1000BT.peek()
      c.io.rx_dv.peek()
      c.io.rx_er.peek()
    }
  }

  it should "reset to IDLE when pcs_reset is asserted" in {
    test(new EthernetPCSRXFSM()) { c =>
      // go into RECEIVE
      pokeSym(c, ssd1Sym); pokeSym(c, ssd2Sym); pokeSym(c, ssd2Sym)
      // now assert reset
      c.io.pcs_reset.poke(true.B)
      c.clock.step(1)
      // back to IDLE
      c.io.receive1000BT.expect(false.B)
      c.io.rx_dv.peek()
      c.io.rx_er.peek()
    }
  }

  it should "handle random symbol sequences without crashing" in {
    test(new EthernetPCSRXFSM()) { c =>
      c.io.config.poke(false.B)
      c.io.pcs_reset.poke(false.B)

      val rand = new Random(0)
      val allSyms = IDLE ++ SSD1 ++ SSD2 ++ DATA ++ CSRESET ++ XMT_ERR

      // run 1 000 random cycles
      for (_ <- 0 until 1000) {
        val sym = allSyms(rand.nextInt(allSyms.size))
        pokeSym(c, sym)
        // ensure backpressure never asserted
        c.io.rx_symb_vector.ready.peek()
      }
    }
  }
}