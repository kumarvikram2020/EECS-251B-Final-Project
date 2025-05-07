package ethernet_pcs

import chisel3._
import chisel3.util._
//import org.chipsalliance.cde.config.{Parameters, Field, Config}
//import freechips.rocketchip.diplomacy._
//import freechips.rocketchip.regmapper._
//import freechips.rocketchip.subsystem._
//import freechips.rocketchip.tilelink._

import ethernet_pcs._
import PCSCodes._


/*class SideStreamScrambler() extends Module {
    val io = IO(new Bundle { 
        val enable = Input(Bool())
        val config = Input(Bool())
        val resetScrambler = Input(Bool())
        val txd = Input(UInt(8.W))
        val tx_enn_2 = Input(Bool())
        val tx_err = Input(Bool())
        val loc_lpi_req = Input(Bool())
        val loc_update_done = Input(Bool())
        val loc_rcvr_status = Input(Bool())
        val tx_mode = Input(UInt(2.W))
        val outBit = Output(Bool())
        val sdn = Output(UInt(9.W))
    })
    val SEND_Z = 2.U(2.W)

    val scr = RegInit("b1_1000_1011_0111_1110_0101_1111_0000_0001_0110_0111_1010".U(33.W))
    val Xn = RegInit("b0".U(1.W))
    val Yn = RegInit("b0".U(1.W))
    val csn = RegInit("b000".U(3.W))
    val TXDn = RegInit(0.U(8.W))

    val Sxn = Wire(UInt(4.W))
    val Syn = Wire(UInt(4.W))
    val Sgn = Wire(UInt(4.W))

    val Syn_1 = RegInit("b0000".U(4.W))
    Syn_1 := Syn

    val n0 = RegInit(0.U(8.W))
    val n = RegInit(0.U(8.W))
    n := n + 1.U

    when (io.resetScrambler) {
        n0 := n
    }

    val tx_enablen_2 = RegInit(false.B)

    val Scn = Wire(UInt(8.W))
    val Sdn = Wire(UInt(9.W))

    when (io.enable) {
        when (io.config) {
            scr := Cat(scr(31, 0), scr(12) ^ scr(32))
        }.otherwise {
            scr := Cat(scr(31, 0), scr(19) ^ scr(32))
        }
        Xn := scr(4) ^ scr(9)
        Yn := scr(1) ^ scr(5)
    }

    Syn := Cat(scr(0), scr(3) ^ scr(8), scr(6) ^ scr(16), scr(9) ^ scr(14) ^ scr(19) ^ scr(24))
    Sxn := Cat(scr(4) ^ scr(6), scr(7) ^ scr(9) ^ scr(12) ^ scr(14), scr(10) ^ scr(12) ^ scr(20) ^ scr(22), scr(13) ^ scr(15) ^ scr(18) ^ scr(20) ^ scr(23) ^ scr(25) ^ scr(28) ^ scr(30))
    Sgn := Cat(Yn, scr(4) ^ scr(8) ^ scr(9) ^ scr(13), scr(7) ^ scr(11) ^ scr(17) ^ scr(21), scr(10) ^ scr(14) ^ scr(15) ^ scr(19) ^ scr(20) ^ scr(24) ^ scr(25) ^ scr(29))

    // Scn[7:4]
    when (io.tx_enn_2 === 1.U) {
        Scn(7, 4) := Sxn(3, 0)
    }.otherwise {
        Scn(7, 4) := 0.U(4.W)
    }

    // Scn[3:1]
    when (io.tx_mode === SEND_Z) {
        Syn(3, 1) := 0.U(3.W)
    }.elsewhen ((n - n0) % 2.U === 0.U) {
        Scn(3, 1) := Syn(3, 1)
    }.otherwise {
        Scn(3, 1) := Syn_1(3, 1) ^ ("b111".U(3.W))
    }

    // Scn[0]
    when (io.tx_mode === SEND_Z) {
        Scn(0) := 0.U(1.W)
    }.otherwise {
        Scn(0) := Syn(0)
    }

    val csresetn = (io.tx_enn_2 === 1.U(1.W)) && (io.enable === 0.U(1.W))
    val cext_n = Wire(UInt(1.W))
    val cext_err_n = Wire(UInt(1.W))


    // Logic for Sdn[8]

    Sdn(8) := csn(0) // Sdn[8] is derived from csn[0]

    // Logic for Sdn[7:6]
    when (io.tx_enn_2 === 1.U && io.resetScrambler === 0.U) {
        Sdn(7, 6) := Scn(7, 6) ^ TXDn(7, 6)
    }.elsewhen(csresetn === 1.U) {
        Sdn(7, 6) := csn(1, 0)
    }.otherwise {
        Sdn(7, 6) := Scn(7, 6) // If not enabled, use only Scn[7:6]
    }

    // Logic for Sdn[5:4]
    when (io.tx_enn_2 === 1.U) {
        Sdn(5, 4) := Scn(5, 4) ^ TXDn(5, 4)
    }.otherwise {
        Sdn(5, 4) := Scn(5, 4) // If not enabled, use only Scn[5:4]
    }

    // Logic for Sdn[3] (data scrambling or loc_lpi_req encoding)
    when (io.tx_enn_2 === 1.U) {
        Sdn(3) := Scn(3) ^ TXDn(3)
    }.elsewhen(io.loc_lpi_req === true.B && io.tx_mode =/= SEND_Z) {
        Sdn(3) := Scn(3) ^ 1.U
    }.otherwise {
        Sdn(3) := Scn(3)
    }

    // Logic for Sdn[2] (scrambling or loc_rcvr_status encoding)
    when (io.tx_enn_2 === 1.U) {
        Sdn(2) := Scn(2) ^ TXDn(2)
    }.elsewhen(io.loc_rcvr_status === true.B && io.tx_mode =/= SEND_Z) {
        Sdn(2) := Scn(2) ^ 1.U
    }.otherwise {
        Sdn(2) := Scn(2)  // If not enabled, set to 0 (default behavior)
    }

    when (io.enable === 0.U && io.txd === "0x0F".U(8.W)) {
        cext_n := io.tx_err
    }.otherwise {
        cext_n := 0.U
    }

    when (io.enable === 0.U && io.loc_lpi_req === false.B && io.txd =/= "0x0F".U(8.W)) {
        cext_err_n := io.tx_err
    }.otherwise {
        cext_err_n := 0.U
    }

    // Logic for Sdn[1] (carrier extension and tx_errorn handling)
    when (io.tx_enn_2 === 1.U) {
        Sdn(1) := Scn(1) ^ TXDn(1) // Use carrier extension bits if in SEND_N mode
    }.elsewhen (io.loc_update_done === true.B && io.tx_mode =/= SEND_Z) {
        Sdn(1) := Scn(1) ^ 1.U // Error indication for transmission error (symbol substitution)
    }.otherwise {
        Sdn(1) := Scn(1) ^ cext_err_n // Otherwise, use Scn[1:0]
    }

    // Logic for Sdn[0]
    when (io.tx_enn_2 === 1.U) {
        Sdn(0) := Scn(0) ^ TXDn(0)
    }.otherwise {
        Sdn(0) := Scn(0) ^ cext_n
    }

    // Output Sdn[8:0]
    io.sdn := Sdn  // Assign the final Sdn[8:0] value to the output

    // Logic to update convolutional encoder state (csn)
    when (io.enable) {
        csn(0) := csn(2)
        csn(1) := Sdn(6) ^ csn(0)
        csn(2) := Sdn(7) ^ csn(1)
    }

    io.outBit := scr(0)
}*/

class EthernetTXFSMIO() extends Bundle {
    
    // Input
    val config = Input(Bool()) 
    val indication = Input(UInt(2.W))
    val pcs_reset = Input(Bool())
    val tx_er = Input(Bool())
    val tx_en = Input(Bool())
    val txd = Input(UInt(8.W))
    val sdn = Input(UInt(9.W))
    val receive1000BT = Input(Bool())
    val tx_symb_vector_encoded = Input(Vec(4, SInt(3.W)))
    /*val loc_rcvr_status = Input(Bool())
    val loc_update_done = Input(Bool())*/

    // Output
    val col = Output(Bool())
    val tx_symb_vector = DecoupledIO(Vec(4, SInt(3.W)))
}

class EthernetPCSTXFSM() extends Module {
    val io = IO(new EthernetTXFSMIO())
    val sideStreamScrambler = Module(new SideStreamScrambler())
    
    val sIdle :: ssd1 :: ssd2 :: ssd1Err :: ssd2Err :: transmitErr :: transmitData :: esd1 :: esd2 :: firstCsReset :: secondCsReset :: Nil = Enum(11) 
    val state = RegInit(sIdle)
    val idleCntr = RegInit(0.U(4.W))

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
            io.tx_symb_vector.bits.enq(ESD1(0))
        }
        is(esd2) {
            transmit1000BTWire := false.B
            io.col := false.B 
            io.tx_symb_vector.bits.enq(ESD2(0))
        }
        is(firstCsReset){
            transmit1000BTWire := false.B
            io.col := false.B 
            io.tx_symb_vector.bits.enq(CSRESET(0))
        }
        is(secondCsReset){
            transmit1000BTWire := false.B
            io.col := false.B 
            io.tx_symb_vector.bits.enq(CSRESET(0))
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
                stater := secondCsReset
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