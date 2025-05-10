package ethernet_pcs

import chisel3._
import chisel3.util._
import org.chipsalliance.cde.config.{Parameters, Field, Config}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.regmapper._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._

trait EthernetPCSTopIO extends Bundle {}

trait EthernetPCSTop extends HasRegMap {
  val io: EthernetPCSTopIO
  val clock: Clock
  val reset: Reset

  val pcs_reset = RegInit(false.B)
  val tx_en = RegInit(false.B)
  val tx_er = RegInit(false.B)
  val txd = RegInit(0.U(8.W))
  val tx_symb_vector_encoded = RegInit(0.U(12.W)) 
  val config = RegInit(false.B)
  val indication = RegInit(0.U(2.W))

  val rx_symb_vector = RegInit(0.U(12.W)) 
  val decoded_rx_symb_vector = RegInit(0.U(8.W))

  val rx = withReset(reset) {
    Module(new EthernetPCSRXFSM())
  }
  val tx = withReset(reset) {
    Module(new EthernetPCSTXFSM())
  }

  // tx connection
  tx.io.pcs_reset <> pcs_reset
  tx.io.tx_en <> tx_en
  tx.io.tx_er <> tx_er
  tx.io.txd <> txd
  tx.io.tx_symb_vector_encoded <> VecInit(
    tx_symb_vector_encoded.asBools.grouped(3).map { bits =>
      Cat(bits.reverse).asSInt
    }.toSeq
  )
  tx.io.config <> config
  tx.io.indication <> indication
  tx.io.tx_symb_vector.ready <> true.B

  tx.io.receive1000BT <> rx.io.receive1000BT

  // rx connection
  rx.io.config <> config
  rx.io.pcs_reset <> pcs_reset
  rx.io.rx_symb_vector.enq(
    VecInit(
      rx_symb_vector.asBools.grouped(3).map { bits =>
        Cat(bits.reverse).asSInt
      }.toSeq
    )
  )
  rx.io.from_decoder_symbols <> decoded_rx_symb_vector
  
  regmap(
    0x00 -> Seq(RegField.w(1, pcs_reset)),                     
    0x04 -> Seq(RegField.w(1, tx_en)),                     
    0x08 -> Seq(RegField.w(1, tx_er)),                      
    0x0C -> Seq(RegField.w(8, txd)), 
    0x10 -> Seq(RegField.w(12, tx_symb_vector_encoded)), 
    0x14 -> Seq(RegField.w(1, config)), 
    0x1C -> Seq(RegField.w(1, indication)), 
    0x20 -> Seq(RegField.w(12, rx_symb_vector)),
    0x24 -> Seq(RegField.w(8, decoded_rx_symb_vector)),                          
    0x28 -> Seq(RegField.r(1, tx.io.col)),    
    0x2C -> Seq(RegField.r(12, tx.io.tx_symb_vector.bits.asUInt)),                     
    0x30 -> Seq(RegField.r(8, tx.io.state_test)),
    0x34 -> Seq(RegField.r(1, rx.io.rxerror_status)),
    0x38 -> Seq(RegField.r(1, rx.io.rx_dv)),
    0x3C -> Seq(RegField.r(1, rx.io.rx_er)),
    0x40 -> Seq(RegField.r(12, rx.io.rxd))          
  )
}

class EthernetPCSTL(params: EthernetPCSParams, beatBytes: Int)(implicit p: Parameters)
  extends TLRegisterRouter(
    params.address, "gmii_rx_tx", Seq("eecs251b,gmii_rx_tx"),
    beatBytes = beatBytes)(
      new TLRegBundle(params, _) with EthernetPCSTopIO)(
      new TLRegModule(params, _, _) with EthernetPCSTop)


case class EthernetPCSParams(
  address: BigInt = 0x8000
)

case object EthernetPCSKey extends Field[Option[EthernetPCSParams]](None)

trait CanHavePeripheryEthernetPCS { this: BaseSubsystem =>
  private val portName = "gmii_rx_tx"

  val gmii_rx_tx = p(EthernetPCSKey) match {
    case Some(params) => {
      val gmii_rx_tx = LazyModule(new EthernetPCSTL(params, pbus.beatBytes)(p))
      pbus.coupleTo(portName) { gmii_rx_tx.node := TLFragmenter(pbus.beatBytes, pbus.blockBytes) := _ }
      Some(gmii_rx_tx)
    }
    case None => None
  }
}

trait CanHavePeripheryEthernetPCSImp extends LazyModuleImp {
  val outer: CanHavePeripheryEthernetPCS
}

class WithEthernetPCS(params: EthernetPCSParams) extends Config((site, here, up) => {
  case EthernetPCSKey => Some(params)
})