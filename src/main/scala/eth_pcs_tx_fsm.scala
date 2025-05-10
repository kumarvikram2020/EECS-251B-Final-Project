package ethernet_pcs

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage


import ethernet_pcs._
import PCSCodes._

class EthernetTXFSMIO() extends Bundle {
    
    // Input
    val config = Input(Bool()) 
    val indication = Input(UInt(2.W))
    val pcs_reset = Input(Bool())
    val tx_er = Input(Bool())
    val tx_en = Input(Bool())
    val txd = Input(UInt(8.W))
    val receive1000BT = Input(Bool())
    val tx_symb_vector_encoded = Input(Vec(4, SInt(3.W)))

    // Output
    val col = Output(Bool())
    val tx_symb_vector = DecoupledIO(Vec(4, SInt(3.W)))
    val state_test = Output(UInt(4.W))
}

class EthernetPCSTXFSM() extends Module {
    val io = IO(new EthernetTXFSMIO())
    // val sideStreamScrambler = Module(new SideStreamScrambler())
    
    val sIdle :: ssd1 :: ssd2 :: ssd1Err :: ssd2Err :: transmitErr :: transmitData :: esd1 :: esd2 :: firstCsReset :: secondCsReset :: Nil = Enum(11) 
    val state = RegInit(sIdle)
    val idleCntr = RegInit(0.U(4.W))

    io.state_test := state

    // helpers and registers
    val PUDR = io.tx_symb_vector.fire
    val transmit1000BTWire = WireDefault(false.B)

    io.col := false.B
    io.tx_symb_vector.valid := false.B

    //val tx_enablen_1 = RegInit(false.B)
    //val tx_enablen_2 = RegInit(false.B)

    //val loc_lpi_req = RegInit(false.B)
    //val sLpiOff :: sLpiOn :: Nil = Enum(2)
    //val loc_lpi_req_state = RegInit(sLpiOff)

    //tx_enablen_1 := io.tx_en
    //tx_enablen_2 := tx_enablen_1
    // Scrambler Inputs & Outputs
    /*sideStreamScrambler.io.enable := io.tx_en 
    sideStreamScrambler.io.config := io.config
    sideStreamScrambler.io.resetScrambler := io.pcs_reset 
    sideStreamScrambler.io.tx_mode := io.indication 
    sideStreamScrambler.io.tx_enn_2 := tx_enablen_2
    sideStreamScrambler.io.txd := io.txd 
    sideStreamScrambler.io.tx_err := io.tx_er 
    sideStreamScrambler.io.loc_lpi_req := loc_lpi_req
    sideStreamScrambler.io.loc_rcvr_status := io.loc_rcvr_status
    sideStreamScrambler.io.loc_update_done := io.loc_update_done


    val idx = Wire(UInt(9.W))
    val col = Wire(UInt(3.W))
    when(sideStreamScrambler.io.sdn(6, 8) === 0.U) {
        col := 0.U
    }.elsewhen(sideStreamScrambler.io.sdn(6, 8) === 2.U) {
        col := 1.U
    }.elsewhen(sideStreamScrambler.io.sdn(6, 8) === 4.U) {
        col := 2.U
    }.elsewhen(sideStreamScrambler.io.sdn(6, 8) === 5.U) {
        col := 3.U
    }

    idx := (sideStreamScrambler.io.sdn(5, 0) - 31.U) + col

    val selectedVec = Mux(idx === 0.U, DATA(0),
              Mux(idx === 1.U, DATA(1),
              Mux(idx === 2.U, DATA(2),
              Mux(idx === 3.U, DATA(3), // Continue as needed
              DATA(0)))))  // Default value if idx doesn't match any case*/

    // Assign Signals Per State
    switch(state) {
        is(sIdle) {
           transmit1000BTWire := false.B 
           io.col := false.B
           io.tx_symb_vector.enq(IDLE(0))
        }
        is(ssd1){
           transmit1000BTWire := true.B
           io.col := io.receive1000BT
           io.tx_symb_vector.enq(SSD1(0))
        }
        is(ssd2){
            transmit1000BTWire := true.B
            io.col := io.receive1000BT
            io.tx_symb_vector.enq(SSD2(0))
        }
        is(ssd1Err){
            transmit1000BTWire := true.B 
            io.col := io.receive1000BT
            io.tx_symb_vector.enq(SSD1(0))
        }
        is(ssd2Err){
            transmit1000BTWire := true.B 
            io.col := io.receive1000BT 
            io.tx_symb_vector.enq(SSD2(0))
        }
        is(transmitErr){
            transmit1000BTWire := true.B 
            io.col := io.receive1000BT 
            io.tx_symb_vector.enq(XMT_ERR(0))
        }
        is(transmitData){
            transmit1000BTWire := true.B
            io.col := io.receive1000BT
            io.tx_symb_vector.enq(io.tx_symb_vector_encoded)
        }
        is(esd1){
            transmit1000BTWire := false.B
            io.col := false.B 
            io.tx_symb_vector.enq(ESD1(0))
        }
        is(esd2) {
            transmit1000BTWire := false.B
            io.col := false.B 
            io.tx_symb_vector.enq(ESD2(0))
        }
        is(firstCsReset){
            transmit1000BTWire := false.B
            io.col := false.B 
            io.tx_symb_vector.enq(CSRESET(0))
        }
        is(secondCsReset){
            transmit1000BTWire := false.B
            io.col := false.B 
            io.tx_symb_vector.enq(CSRESET(0))
        }
    }

    // State Transition Logic
    switch(state) {
        is(sIdle) {
            when (!io.tx_en && PUDR) {
                state := sIdle
            }.elsewhen (io.tx_en && io.tx_er && PUDR) {
                state := ssd1Err
            }.elsewhen (io.tx_en && !io.tx_er && PUDR) {
                state := ssd1
            }.otherwise {
                state := sIdle
            }
        }
        is(ssd1){
            when (!io.tx_er && PUDR) {
                state := ssd2 
            }.elsewhen(io.tx_er && PUDR) {
                state := ssd2Err
            }.otherwise {
                state := ssd1
            }
        }
        is(ssd2){
            when (io.tx_en && io.tx_er && PUDR) {
                state := transmitErr
            }.elsewhen (io.tx_en && !io.tx_er && PUDR) {
                state := transmitData
            }.elsewhen (!io.tx_en && !io.tx_er && PUDR) {
                state := firstCsReset
            }.otherwise {
                state := ssd2
            }
        }
        is(ssd1Err){
            when (PUDR) {
                state := ssd2Err
            }.otherwise {
                state := ssd1Err
            }
        }
        is(ssd2Err){
            when (PUDR) {
                state := transmitErr
            }.otherwise {
                state := ssd2Err
            }
        }
        is(transmitErr){
            when (io.tx_en && io.tx_er && PUDR) {
                state := transmitErr
            }.elsewhen (io.tx_en && !io.tx_er && PUDR) {
                state := transmitData
            }.elsewhen (!io.tx_en && !io.tx_er && PUDR) {
                state := firstCsReset
            }.otherwise {
                state := transmitErr
            }
        }
        is(transmitData){
           when (io.tx_en && io.tx_er && PUDR) {
                state := transmitErr
            }.elsewhen (io.tx_en && !io.tx_er && PUDR) {
                state := transmitData
            }.elsewhen (!io.tx_en && !io.tx_er && PUDR) {
                state := firstCsReset
            }.otherwise {
                state := transmitData
            }
        }
        is(esd1){
            when (PUDR) {
                state := esd2
            }.otherwise {
                state := esd1
            }
        }
        is(esd2) {
            when (PUDR) {
                state := sIdle
            }.otherwise {
                state := esd2
            }
        } 
        is (firstCsReset) {
            when (PUDR) {
                state := secondCsReset
            }.otherwise {
                state := firstCsReset
            }
        }
        is (secondCsReset) {
            when (PUDR) {
                state := esd1
            }.otherwise {
                state := secondCsReset
            }
        }
    }

    /*switch (loc_lpi_req_state) {
        is(sLpiOff) {
            when (io.tx_en === false.B && io.tx_er === true.B && io.txd === "0x01".U(8.W) && transmit1000BTWire === false.B) {
                loc_lpi_req_state := sLpiOn
            }
        }
        is (sLpiOn) {
            when (io.tx_en === true.B && io.tx_er === false.B && io.txd =/= "0x01".U(8.W) && transmit1000BTWire === true.B) {
                loc_lpi_req_state := sLpiOff
            }
        }
    }

    switch (loc_lpi_req_state) {
        is(sLpiOff) {
            loc_lpi_req := false.B
        }
        is (sLpiOn) {
            loc_lpi_req := true.B
        }
    }*/
}