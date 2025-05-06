package ethernet_pcs

import chisel3._
import chisel3.util._
import org.chipsalliance.cde.config.{Parameters, Field, Config}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.regmapper._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._

import ethernet_pcs._
import PCSCodes._

class EthernetRXFSMIO() extends Bundle {
    
    // Input
    val config = Input(Bool())                                      // Indicates whether the PHY must operate as a MASTER PHY or as a SLAVE PHY
    val pcs_reset = Input(Bool())                                   // PCS Reset Signal -- reset the FSM
    val rx_symb_vector = Flipped(DecoupledIO(Vec(4, SInt(3.W))))    // Quinary symbols from PMA
    val from_decoder_symbols = Input(UInt(8.W))

    // Output
    val receive1000BT = Output(Bool())
    val rxerror_status = Output(Bool())
    val rxd = Output(UInt(8.W))                          // RX Data
    val rx_dv = Output(Bool())                           // RX Data Valid
    val rx_er = Output(Bool())                           // RX Data Error
}

class EthernetPCSRXFSM() extends Module {
    val io = IO(new EthernetRXFSMIO())

    val sIDLE :: sNON_IDLE_DETECT :: sSSD1_VECTOR :: sSSD2_VECTOR :: sBAD_SSD :: sRECEIVE :: sPREMATURE_END :: sDATA_ERROR :: sDATA :: sCS_RESET_1 :: sCS_RESET_2 :: sESD_IDLE_1 :: Nil = Enum(12)
    val state = RegInit(sIDLE)

    // helpers and registers
    val PUDI = io.rx_symb_vector.valid
    val rx_symb_vector_prev = ShiftRegister(io.rx_symb_vector.bits, 1, io.rx_symb_vector.valid) // 1 cycle delay and shifts into register when data is valid
    val rx_symb_vector_current = io.rx_symb_vector.bits

    // state outputs
    // default values
    io.receive1000BT := false.B
    io.rxerror_status := false.B
    io.rx_dv := false.B
    io.rx_er := false.B
    io.rxd := "h00".U
    io.rx_symb_vector.ready := true.B

    switch(state) {
        is(sIDLE) {
            // uses default values
        }
        is(sNON_IDLE_DETECT){
            io.receive1000BT := true.B            
        }
        is(sBAD_SSD){
            io.receive1000BT := true.B
            io.rxerror_status := true.B
            io.rx_er := true.B
            io.rxd := "h0E".U          
        }
        is(sSSD1_VECTOR){
            io.receive1000BT := true.B
            io.rx_dv := true.B
            io.rxd := "h55".U          
        }
        is(sSSD2_VECTOR){
            io.receive1000BT := true.B
            io.rx_dv := true.B
            io.rxd := "h55".U          
        }
        is(sRECEIVE){
            io.receive1000BT := true.B
            io.rx_dv := true.B
            io.rxd := "h55".U         
        }
        is(sPREMATURE_END){
            io.receive1000BT := true.B
            io.rx_dv := true.B
            io.rx_er := true.B
            io.rxd := "h55".U          
        }
        is(sDATA_ERROR){
            io.receive1000BT := true.B
            io.rx_dv := true.B
            io.rx_er := true.B
            io.rxd := "h55".U          
        }
        is(sDATA){
            io.receive1000BT := true.B
            io.rx_dv := true.B
            io.rx_er := false.B
            io.rxd := io.from_decoder_symbols         
        }
        is(sCS_RESET_1){
            io.receive1000BT := false.B
            io.rxerror_status := false.B
            io.rx_dv := false.B
            io.rx_er := false.B
            io.rxd := io.from_decoder_symbols     
        }
        is(sCS_RESET_2){
            io.receive1000BT := false.B
            io.rxerror_status := false.B
            io.rx_dv := false.B
            io.rx_er := false.B
            io.rxd := io.from_decoder_symbols     
        }
        is(sCS_RESET_2){
            io.receive1000BT := false.B
            io.rxerror_status := false.B
            io.rx_dv := false.B
            io.rx_er := false.B
            io.rxd := io.from_decoder_symbols     
        }
        is(sESD_IDLE_1){
            io.receive1000BT := false.B
            io.rxerror_status := false.B
            io.rx_dv := false.B
            io.rx_er := false.B
            io.rxd := io.from_decoder_symbols     
        }
    }

    // state transition logic
    switch(state) {
        is(sIDLE) {
            when (!isIDLE(rx_symb_vector_current) && PUDI) {
                state := sNON_IDLE_DETECT
            }.otherwise{
                state := sIDLE
            }            
        }
        is(sNON_IDLE_DETECT){
            when (PUDI && isSSD1(rx_symb_vector_prev) && isSSD2(rx_symb_vector_current)) {
                state := sSSD1_VECTOR
            }.elsewhen(PUDI && !isSSD1(rx_symb_vector_prev) || !isSSD2(rx_symb_vector_current)) {
                state := sBAD_SSD
            }.otherwise{
                state := sNON_IDLE_DETECT
            }            
        }
        is(sBAD_SSD){
            when (PUDI && isIDLE(rx_symb_vector_current)) {
                state := sIDLE
            }.otherwise{
                state := sBAD_SSD
            }                    
        }
        is(sSSD1_VECTOR){
            when (PUDI) {
                state := sSSD2_VECTOR
            }.otherwise{
                state := sSSD1_VECTOR
            }          
        }
        is(sSSD2_VECTOR){
            when (PUDI) {
                state := sRECEIVE
            }.otherwise{
                state := sSSD2_VECTOR
            } 
        }
        is(sRECEIVE){
            when (PUDI && isDATA(rx_symb_vector_prev)) {
                state := sDATA
            }.elsewhen (PUDI && isCSRESET(rx_symb_vector_current) && isCSRESET(rx_symb_vector_prev)) {
                state := sCS_RESET_1
            }.elsewhen (PUDI && isXMT_ERR(rx_symb_vector_current)) {
                state := sDATA_ERROR
            }.otherwise{
                state := sPREMATURE_END
            }
        }
        is(sPREMATURE_END){
           when (PUDI && isIDLE(rx_symb_vector_current)) {
            state := sIDLE  
           }.otherwise{
                state := sPREMATURE_END
            }      
        }
        is(sDATA_ERROR){
            when (PUDI) {
                state := sRECEIVE  
            }.otherwise{
                state := sDATA_ERROR
            }     
        }
        is(sDATA){
            when (PUDI) {
                state := sRECEIVE  
            }.otherwise{
                state := sDATA
            }      
        }
        is(sCS_RESET_1){
            when (PUDI) {
                state := sCS_RESET_2  
            }.otherwise{
                state := sCS_RESET_1
            } 
        }
        is(sCS_RESET_2){
            when (PUDI) {
                state := sESD_IDLE_1  
            }.otherwise{
                state := sCS_RESET_2
            }
        }
        is(sESD_IDLE_1){
            when (PUDI) {
                state := sIDLE  
            }.otherwise{
                state := sESD_IDLE_1
            }     
        }
    }
}