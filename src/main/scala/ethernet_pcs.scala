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
class EthernetPCSIO() extends Bundle {
    
    // Input
    val gtx_clk = Input(UInt(1.W))
    val txd = Input(UInt(8.W))
    val tx_en = Input(UInt(1.W))
    val tx_er = Input(UInt(1.W))
    val pma_txmode_indicate = Input(UInt(1.W))
    val pma_config_indicate = Input(UInt(1.W))
    val pma_unidata_indicate = Input(UInt(1.W))
    val pma_rxstatus_indicate = Input(UInt(1.W))
    val pma_reset_indicate = Input(UInt(1.W))

    // Output
    val col = Ouput(UInt(1.W))
    val crs = Ouput(UInt(1.W))
    val rx_clk = Ouput(UInt(1.W))
    val rxd = Ouput(UInt(8.W))
    val rx_dv = Ouput(UInt(1.W))
    val rx_er = Ouput(UInt(1.W))
    val pma_unidata_request = Ouput(UInt(1.W))
    val pma_remrxstatus_request = Ouput(UInt(1.W))
    val pma_scrstatus_request = Ouput(UInt(1.W))
}

class EthernetPCS(params: EthernetPCSParams, pBeatBytes: Int, sBeatBytes: Int) (implicit p: Parameters) extends Module {
    val io = IO(new EthernetPCSIO())

    val mmio_device = new SimpleDevice("PCS", Seq("ucbbar, PCS")) 
    val mmio_node = TLRegisterNode(Seq(AddressSet(params.address, 4096-1)), mmio_device, "reg/control", beatBytes=pBeatBytes)


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