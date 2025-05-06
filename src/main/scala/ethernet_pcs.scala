package ethernet_pcs

import chisel3._
import chisel3.util._
import org.chipsalliance.cde.config.{Parameters, Field, Config}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.regmapper._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._


// IO Declarations
case object EthernetPCSKey extends Field[Option[EthernetPCSParams]](None)

// Parameters for the EthernetPCSTile
case class EthernetPCSParams(
  address: BigInt          // MMIO Address 
  ) 

/* 
    Signals are based on the 802.3ab-1999 spec page 23.
*/
/* 
    Signals are based on the 802.3ab-1999 spec page 18 & 23.
*/
class EthernetPCSTopIO() extends Bundle {
    
    // Input
    val gtx_clk = Input(Bool())                         // 1000T TX Clock
    val txd = Input(UInt(8.W))                          // TX Data
    val tx_en = Input(Bool())                           // TX Enable
    val tx_er = Input(Bool())                           // TX Error
    val rx_symb_vector = Input(Vec(4, UInt(3.W)))       // Quinary symbols from PMA
    val tx_mode = Input(UInt(2.W))                     // Indicates sequence of code-groups the PCS should be transmitting
    val config = Input(Bool())                          // Indicates whether the PHY must operate as a MASTER PHY or as a SLAVE PHY
    val pcs_reset = Input(Bool())                       // PCS Reset Signal
    val loc_rcvr_status = Input(Bool())                 // Link reliability OK or NOT_OK
    val link_status = Input(Bool())                     // Link status from link monitor

    // Output
    val col = Output(Bool())                             // Indications collision
    val crs = Output(Bool())                             // MAC uses for deferral in half duplex mode
    val rx_clk = Output(Bool())                          // RX Clock
    val rxd = Output(UInt(8.W))                          // RX Data
    val rx_dv = Output(Bool())                           // RX Data Valid
    val rx_er = Output(Bool())                           // RX Data Error
    val tx_symb_vector = Output(Vec(4, UInt(3.W)))       // Quinary symbols to PMA
    val rem_rcvr_status = Output(Bool())                 // Indicate the status of the receive link at the remote PHY
    val scr_status = Output(Bool())                      // Descrambler has achieved synchronization or not
}

class EthernetPCS(params: EthernetPCSParams, pBeatBytes: Int, sBeatBytes: Int) (implicit p: Parameters) extends Module {
    val io = IO(new EthernetPCSIO())

    val mmio_device = new SimpleDevice("PCS", Seq("ucbbar, PCS")) 
    val mmio_node = TLRegisterNode(Seq(AddressSet(params.address, 4096-1)), mmio_device, "reg/control", beatBytes=pBeatBytes)

    val sIdle :: ssd1 :: ssd2 :: ssd1Err :: ssd2Err :: transmitErr :: transmitData :: cExt :: errChck :: firstCsExt :: secondCsExt :: esd1 :: esd2_ext_0 :: esd2_ext_1 :: esd1_w_ext :: esd2_w_ext :: firstCsReset :: secondCsReset :: Nil = Enum(19)

    val state = RegInit(sIdle)
    val thousandBTransmit = RegInit(false)
    val tx_symb_vector = RegInit(UInt(8.W))
    val nextState = WireDefault(sIdle)

    switch(state) {
        is (sIdle) {
            io.col = 0.U
            io.crs = 0.U 
            thousandBTransmit := false
            tx_symb_vector := 0.U
            
            when(io.tx_en === 1.U && io.tx_er === 0.U) {
                nextState = ssd1
            }.elsewhen(io.tx_en === 1.U && io.tx_err === 1.U) {
                nextState = ssd1Err
            }
        }
        is (ssd1) {
            when(io.tx_en === 1.U && io.tx_er === 0.U) {
                nextState = ssd2
            }.elsewhen(io.tx_er === 1.U) {
                nextState = transmitErr
            }
        }
        is (ssd1Err) {
            when (io.tx_)
        }
        is (ssd2) {
            when(io.tx_en === 1.U && io.tx_er === 0.U) {
                nextState = transmitData
            }.elsewhen(io.tx_er === 1.U) {
                nextState = transmitErr
            }
        }
        is (ssd2Err) {

        }
        is (transmitErr) {
            when(io.tx_en === 0.U) {
                nextState = sIdle
            }
        }
        is (transmitData) {
            when(io.tx_en === 0.U) {
                nextState = sIdle
            }.elsewhen(io.tx_er === 1.U) {
                nextState = transmitErr
            }
        }
        is (cExt) {
            when(io.tx_en === 0.U) {
                nextState = sIdle
            }.elsewhen(io.tx_er === 1.U) {
                nextState = transmitErr
            }
        }
        is (errChck) {
            when(io.tx_en === 0.U) {
                nextState = sIdle
            }.elsewhen(io.tx_er === 1.U) {
                nextState = transmitErr
            }
        }
        is (firstCsExt) {
            when(io.tx_en === 1.U && io.tx_er === 0.U) {
                nextState = secondCsExt
            }.elsewhen(io.tx_er === 1.U) {
                nextState = transmitErr
            }
        }
        is (secondCsExt) {
            when(io.tx_en === 1.U && io.tx_er === 0.U) {
                nextState = esd1
            }.elsewhen(io.tx_er === 1.U) {
                nextState = transmitErr
            }
        }
        is (esd1) {
            when(io.tx_en === 1.U && io.tx_er === 0.U) {
                nextState = esd2
            }.elsewhen(io.tx_er === 1.U) {
                nextState = transmitErr
            }
        }
        is (esd2) {
            when(io.tx_en === 1.U && io.tx_er === 0.U) {
                nextState = sIdle
            }.elsewhen(io.tx_er === 1.U) {
                nextState = transmitErr
            }
        }
    }

    state := nextState 

}

trait CanHavePeripheryEthernetPCS { this: BaseSubsystem =>
  private val portName = "ethernet_pcs"

  val eth_pcs = p(EthernetPCSKey) match {
    case Some(params) => {
      val eth_pcs = LazyModule(new EthernetPCS(params, pbus.beatBytes, sbus.beatBytes)(p))
      pbus.coupleTo(portName) { eth_pcs.mmio_node := TLFragmenter(pbus.beatBytes, pbus.blockBytes) := _ }
      Some(eth_pcs)
    }
    case None => None
  }
}

class WithEthernetPCS(params: EthernetPCSParams) extends Config((site, here, up) => {
  case EthernetPCSKey => Some(params)
})