package ethernet_pcs

import chisel3._
import chisel3.util._
import freechips.rocketchip.subsystem.{BaseSubsystem, CacheBlockBytes}
import org.chipsalliance.cde.config.{Parameters, Field, Config}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import chisel3.experimental.{IntParam, BaseModule}
import freechips.rocketchip.prci._
import freechips.rocketchip.regmapper.{HasRegMap, RegField}
import freechips.rocketchip.util.UIntIsOneOf

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

class EthernetPCS() (implicit p: Parameters) extends Module {
    val io = IO(new EthernetPCSIO())
}