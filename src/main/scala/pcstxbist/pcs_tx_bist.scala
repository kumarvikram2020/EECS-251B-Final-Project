package srambist

import chisel3._
import chisel3.util.log2Ceil

class EthernetTXFSMTest extends AnyFlatSpec with ChiselScalatestTester {
  "EthernetTXFSM" should "transmit correctly" in {
    test(new EthernetPCSTXFSM()) { c =>
      // Initialize inputs
      c.io.tx_en.poke(true.B)
      c.io.tx_er.poke(false.B)
      c.io.txd.poke("h0F".U(8.W))
      c.io.config.poke(true.B)

      // Check the outputs for the initial state
      c.io.transmit1000BT.expect(false.B)
      c.io.tx_symb_vector.bits.expect(0.U)

      c.step(1)
      c.io.transmit1000BT.expect(true.B)

      // Apply some state transitions
      /*c.io.tx_en.poke(true.B)
      c.io.tx_er.poke(true.B)
      c.io.txd.poke("h01".U(8.W))

      // Verify state changes
      c.io.tx_symb_vector.expect(1.U)
      c.io.transmit1000BT.expect(true.B)*/
    }
  }
}