package ethernet_pcs

import chisel3._
import chisel3.util._

object PCSCodes {
    // Valid quinary symbols
    def DATA: Seq[Vec[SInt]] = Seq(
        VecInit(0.S(3.W), 0.S(3.W), 0.S(3.W), 0.S(3.W)),  VecInit(0.S(3.W), 0.S(3.W), 1.S(3.W), 1.S(3.W)),
        VecInit(0.S(3.W), 1.S(3.W), 1.S(3.W), 0.S(3.W)),  VecInit(0.S(3.W), 1.S(3.W), 0.S(3.W), 1.S(3.W)),

        VecInit(-2.S(3.W), 0.S(3.W), 0.S(3.W), 0.S(3.W)), VecInit(-2.S(3.W), 0.S(3.W), 1.S(3.W), 1.S(3.W)),
        VecInit(-2.S(3.W), 1.S(3.W), 1.S(3.W), 0.S(3.W)), VecInit(-2.S(3.W), 1.S(3.W), 0.S(3.W), 1.S(3.W)),

        VecInit(0.S(3.W), -2.S(3.W), 0.S(3.W), 0.S(3.W)), VecInit(0.S(3.W), -2.S(3.W), 1.S(3.W), 1.S(3.W)),
        VecInit(0.S(3.W), -1.S(3.W), 1.S(3.W), 0.S(3.W)), VecInit(0.S(3.W), -1.S(3.W), 0.S(3.W), 1.S(3.W)),

        VecInit(-2.S(3.W), 2.S(3.W), 0.S(3.W), 0.S(3.W)), VecInit(-2.S(3.W), -2.S(3.W), 1.S(3.W), 1.S(3.W)),
        VecInit(-2.S(3.W), -1.S(3.W), 1.S(3.W), 0.S(3.W)), VecInit(-2.S(3.W), -1.S(3.W), 0.S(3.W), 1.S(3.W)),

        VecInit(0.S(3.W), 0.S(3.W), -2.S(3.W), 0.S(3.W)), VecInit(0.S(3.W), 0.S(3.W), -1.S(3.W), 1.S(3.W)),
        VecInit(0.S(3.W), 1.S(3.W), -1.S(3.W), 0.S(3.W)), VecInit(0.S(3.W), 1.S(3.W), -2.S(3.W), 1.S(3.W)),

        VecInit(-2.S(3.W), 0.S(3.W), -2.S(3.W), 0.S(3.W)), VecInit(-2.S(3.W), 0.S(3.W), -1.S(3.W), 1.S(3.W)),
        VecInit(-2.S(3.W), 1.S(3.W), -1.S(3.W), 0.S(3.W)), VecInit(-2.S(3.W), 1.S(3.W), -2.S(3.W), 1.S(3.W)),

        VecInit(0.S(3.W), -2.S(3.W), -2.S(3.W), 0.S(3.W)), VecInit(0.S(3.W), -2.S(3.W), -1.S(3.W), 1.S(3.W)),
        VecInit(0.S(3.W), -1.S(3.W), -1.S(3.W), 0.S(3.W)), VecInit(0.S(3.W), -1.S(3.W), -2.S(3.W), 1.S(3.W)),

        VecInit(-2.S(3.W), -2.S(3.W), -2.S(3.W), 0.S(3.W)), VecInit(-2.S(3.W), -2.S(3.W), -1.S(3.W), 1.S(3.W)),
        VecInit(-2.S(3.W), -1.S(3.W), -1.S(3.W), 0.S(3.W)), VecInit(-2.S(3.W), -1.S(3.W), -2.S(3.W), 1.S(3.W)),

        VecInit(0.S(3.W), 0.S(3.W), 0.S(3.W), -2.S(3.W)), VecInit(0.S(3.W), 0.S(3.W), 1.S(3.W), -1.S(3.W)),
        VecInit(0.S(3.W), 1.S(3.W), 1.S(3.W), -2.S(3.W)), VecInit(0.S(3.W), 1.S(3.W), 0.S(3.W), -1.S(3.W)),

        VecInit(-2.S(3.W), 0.S(3.W), 0.S(3.W), -2.S(3.W)), VecInit(-2.S(3.W), 0.S(3.W), 1.S(3.W), -1.S(3.W)),
        VecInit(-2.S(3.W), 1.S(3.W), 1.S(3.W), -2.S(3.W)), VecInit(-2.S(3.W), 1.S(3.W), 0.S(3.W), -2.S(3.W)),

        VecInit(0.S(3.W), -2.S(3.W), 0.S(3.W), -2.S(3.W)), VecInit(0.S(3.W), -2.S(3.W), 1.S(3.W), -1.S(3.W)),
        VecInit(0.S(3.W), -1.S(3.W), 1.S(3.W), -2.S(3.W)), VecInit(0.S(3.W), -1.S(3.W), 0.S(3.W), -1.S(3.W)),

        VecInit(-2.S(3.W), -2.S(3.W), 0.S(3.W), -2.S(3.W)), VecInit(-2.S(3.W), -2.S(3.W), 1.S(3.W), -1.S(3.W)),
        VecInit(-2.S(3.W), -1.S(3.W), 1.S(3.W), -2.S(3.W)), VecInit(-2.S(3.W), -1.S(3.W), 0.S(3.W), -1.S(3.W)),

        VecInit(0.S(3.W), 0.S(3.W), -2.S(3.W), -2.S(3.W)), VecInit(0.S(3.W), 0.S(3.W), -1.S(3.W), -1.S(3.W)),
        VecInit(0.S(3.W), 1.S(3.W), -1.S(3.W), -2.S(3.W)), VecInit(0.S(3.W), 1.S(3.W), -2.S(3.W), -1.S(3.W)),
        
        VecInit(-2.S(3.W), 0.S(3.W), -2.S(3.W), -2.S(3.W)), VecInit(-2.S(3.W), 0.S(3.W), -1.S(3.W), -1.S(3.W)),
        VecInit(-2.S(3.W), 1.S(3.W), -1.S(3.W), -2.S(3.W)), VecInit(-2.S(3.W), 1.S(3.W), -2.S(3.W), -1.S(3.W)),

        VecInit(0.S(3.W), -2.S(3.W), -2.S(3.W), -2.S(3.W)), VecInit(0.S(3.W), -2.S(3.W), -1.S(3.W), -1.S(3.W)),
        VecInit(0.S(3.W), -1.S(3.W), -1.S(3.W), -2.S(3.W)), VecInit(0.S(3.W), -1.S(3.W), -2.S(3.W), -1.S(3.W)),

        VecInit(-2.S(3.W), -2.S(3.W), -2.S(3.W), -2.S(3.W)), VecInit(-2.S(3.W), -2.S(3.W), -1.S(3.W), -1.S(3.W)),
        VecInit(-2.S(3.W), -1.S(3.W), -1.S(3.W), -2.S(3.W)), VecInit(-2.S(3.W), -1.S(3.W), -2.S(3.W), -1.S(3.W)),

        VecInit(1.S(3.W), 1.S(3.W), 1.S(3.W), 1.S(3.W)), VecInit(1.S(3.W), 1.S(3.W), 0.S(3.W), 0.S(3.W)),
        VecInit(1.S(3.W), 0.S(3.W), 0.S(3.W), 1.S(3.W)), VecInit(1.S(3.W), 0.S(3.W), 1.S(3.W), 0.S(3.W)),

        VecInit(-1.S(3.W), 1.S(3.W), 1.S(3.W), 1.S(3.W)), VecInit(-1.S(3.W), 1.S(3.W), 0.S(3.W), 0.S(3.W)),
        VecInit(-1.S(3.W), 0.S(3.W), 0.S(3.W), 1.S(3.W)), VecInit(-1.S(3.W), 0.S(3.W), 1.S(3.W), 0.S(3.W)),     

        VecInit(1.S(3.W), -1.S(3.W), 1.S(3.W), 1.S(3.W)), VecInit(1.S(3.W), -1.S(3.W), 0.S(3.W), 0.S(3.W)),
        VecInit(1.S(3.W), -2.S(3.W), 0.S(3.W), 1.S(3.W)), VecInit(1.S(3.W), -2.S(3.W), 1.S(3.W), 0.S(3.W)),

        VecInit(-1.S(3.W), -1.S(3.W), 1.S(3.W), 1.S(3.W)), VecInit(-1.S(3.W), -1.S(3.W), 0.S(3.W), 0.S(3.W)),
        VecInit(-1.S(3.W), -2.S(3.W), 0.S(3.W), 1.S(3.W)), VecInit(-1.S(3.W), -2.S(3.W), 1.S(3.W), 0.S(3.W)),

        VecInit(1.S(3.W), 1.S(3.W), -1.S(3.W), 1.S(3.W)), VecInit(1.S(3.W), 1.S(3.W), -2.S(3.W), 0.S(3.W)),
        VecInit(1.S(3.W), 0.S(3.W), -2.S(3.W), 1.S(3.W)), VecInit(1.S(3.W), 0.S(3.W), -1.S(3.W), 0.S(3.W)),

        VecInit(-1.S(3.W), 1.S(3.W), -1.S(3.W), 1.S(3.W)), VecInit(-1.S(3.W), 1.S(3.W), -2.S(3.W), 0.S(3.W)),
        VecInit(-1.S(3.W), 0.S(3.W), -2.S(3.W), 1.S(3.W)), VecInit(-1.S(3.W), 0.S(3.W), -1.S(3.W), 0.S(3.W)),

        VecInit(1.S(3.W), -1.S(3.W), -1.S(3.W), 1.S(3.W)), VecInit(1.S(3.W), -1.S(3.W), -2.S(3.W), 0.S(3.W)),
        VecInit(1.S(3.W), -2.S(3.W), -2.S(3.W), 1.S(3.W)), VecInit(1.S(3.W), -2.S(3.W), -1.S(3.W), 0.S(3.W)),

        VecInit(-1.S(3.W), -1.S(3.W), -1.S(3.W), 1.S(3.W)), VecInit(-1.S(3.W), -1.S(3.W), -2.S(3.W), 0.S(3.W)),
        VecInit(-1.S(3.W), -2.S(3.W), -2.S(3.W), 1.S(3.W)), VecInit(-1.S(3.W), -2.S(3.W), -1.S(3.W), 0.S(3.W)),

        VecInit(1.S(3.W), 1.S(3.W), 1.S(3.W), -1.S(3.W)), VecInit(1.S(3.W), 1.S(3.W), 0.S(3.W), -2.S(3.W)),
        VecInit(1.S(3.W), 0.S(3.W), 0.S(3.W), -1.S(3.W)), VecInit(1.S(3.W), 0.S(3.W), 1.S(3.W), -2.S(3.W)),

        VecInit(-1.S(3.W), 1.S(3.W), 1.S(3.W), -1.S(3.W)), VecInit(-1.S(3.W), 1.S(3.W), 0.S(3.W), -2.S(3.W)),
        VecInit(-1.S(3.W), 0.S(3.W), 0.S(3.W), -1.S(3.W)), VecInit(-11.S(3.W), 0.S(3.W), 1.S(3.W), -2.S(3.W)),

        VecInit(1.S(3.W), -1.S(3.W), 1.S(3.W), -1.S(3.W)), VecInit(1.S(3.W), -1.S(3.W), 0.S(3.W), -2.S(3.W)),
        VecInit(1.S(3.W), -2.S(3.W), 0.S(3.W), -1.S(3.W)), VecInit(1.S(3.W), -2.S(3.W), 1.S(3.W), -2.S(3.W)),

        VecInit(-1.S(3.W), -1.S(3.W), 1.S(3.W), -1.S(3.W)), VecInit(-1.S(3.W), -1.S(3.W), 0.S(3.W), -2.S(3.W)),
        VecInit(-1.S(3.W), -2.S(3.W), 0.S(3.W), -1.S(3.W)), VecInit(-1.S(3.W), -2.S(3.W), 1.S(3.W), -2.S(3.W)),

        VecInit(1.S(3.W), 1.S(3.W), -1.S(3.W), -1.S(3.W)), VecInit(1.S(3.W), 1.S(3.W), -2.S(3.W), -2.S(3.W)),
        VecInit(1.S(3.W), 0.S(3.W), -2.S(3.W), -1.S(3.W)), VecInit(1.S(3.W), 0.S(3.W), -1.S(3.W), -2.S(3.W)),

        VecInit(-1.S(3.W), 1.S(3.W), -1.S(3.W), -1.S(3.W)), VecInit(-1.S(3.W), 1.S(3.W), -2.S(3.W), -2.S(3.W)),
        VecInit(-1.S(3.W), 0.S(3.W), -2.S(3.W), -1.S(3.W)), VecInit(-1.S(3.W), 0.S(3.W), -1.S(3.W), -2.S(3.W)),

        VecInit(1.S(3.W), -1.S(3.W), -1.S(3.W), -1.S(3.W)), VecInit(1.S(3.W), -1.S(3.W), -2.S(3.W), -2.S(3.W)),
        VecInit(1.S(3.W), -2.S(3.W), -2.S(3.W), -1.S(3.W)), VecInit(1.S(3.W), -2.S(3.W), -1.S(3.W), -2.S(3.W)),

        VecInit(-1.S(3.W), -1.S(3.W), -1.S(3.W), -1.S(3.W)), VecInit(-1.S(3.W), -1.S(3.W), -2.S(3.W), -2.S(3.W)),
        VecInit(-1.S(3.W), -2.S(3.W), -2.S(3.W), -1.S(3.W)), VecInit(-1.S(3.W), -2.S(3.W), -1.S(3.W), -2.S(3.W)),

        VecInit(2.S(3.W), 0.S(3.W), 0.S(3.W), 0.S(3.W)), VecInit(2.S(3.W), 0.S(3.W), 1.S(3.W), 1.S(3.W)),
        VecInit(2.S(3.W), 1.S(3.W), 1.S(3.W), 0.S(3.W)), VecInit(2.S(3.W), 1.S(3.W), 0.S(3.W), 1.S(3.W)),

        VecInit(2.S(3.W), -2.S(3.W), 0.S(3.W), 0.S(3.W)), VecInit(2.S(3.W), -2.S(3.W), 1.S(3.W), 1.S(3.W)),
        VecInit(2.S(3.W), -1.S(3.W), 1.S(3.W), 0.S(3.W)), VecInit(2.S(3.W), -1.S(3.W), 0.S(3.W), 1.S(3.W)),

        VecInit(2.S(3.W), 0.S(3.W), -2.S(3.W), 0.S(3.W)), VecInit(2.S(3.W), 0.S(3.W), -1.S(3.W), 1.S(3.W)),
        VecInit(2.S(3.W), 1.S(3.W), -1.S(3.W), 0.S(3.W)), VecInit(2.S(3.W), 1.S(3.W), -2.S(3.W), 1.S(3.W)),

        VecInit(-2.S(3.W), -2.S(3.W), -2.S(3.W), 0.S(3.W)), VecInit(2.S(3.W), -2.S(3.W), -1.S(3.W), 1.S(3.W)),
        VecInit(2.S(3.W), -1.S(3.W), -1.S(3.W), 0.S(3.W)), VecInit(2.S(3.W), -1.S(3.W), -2.S(3.W), 1.S(3.W)),

        VecInit(2.S(3.W), 0.S(3.W), 0.S(3.W), -2.S(3.W)), VecInit(2.S(3.W), 0.S(3.W), 1.S(3.W), -1.S(3.W)),
        VecInit(2.S(3.W), 1.S(3.W), 1.S(3.W), -2.S(3.W)), VecInit(2.S(3.W), 1.S(3.W), 0.S(3.W), -1.S(3.W)),

        VecInit(2.S(3.W), -2.S(3.W), 0.S(3.W), -2.S(3.W)), VecInit(2.S(3.W), -2.S(3.W), 1.S(3.W), -1.S(3.W)),
        VecInit(2.S(3.W), -1.S(3.W), 1.S(3.W), -2.S(3.W)), VecInit(2.S(3.W), -1.S(3.W), 0.S(3.W), -1.S(3.W)),

        VecInit(2.S(3.W), 0.S(3.W), -2.S(3.W), -2.S(3.W)), VecInit(2.S(3.W), 0.S(3.W), -1.S(3.W), -1.S(3.W)),
        VecInit(2.S(3.W), 1.S(3.W), -1.S(3.W), -2.S(3.W)), VecInit(2.S(3.W), 1.S(3.W), -2.S(3.W), -1.S(3.W)),

        VecInit(2.S(3.W), -2.S(3.W), -2.S(3.W), -2.S(3.W)), VecInit(2.S(3.W), -2.S(3.W), -1.S(3.W), -1.S(3.W)),
        VecInit(2.S(3.W), -1.S(3.W), -1.S(3.W), -2.S(3.W)), VecInit(2.S(3.W), -1.S(3.W), -2.S(3.W), -1.S(3.W)),

        VecInit(0.S(3.W), 0.S(3.W), 2.S(3.W), 0.S(3.W)), VecInit(1.S(3.W), 1.S(3.W), 2.S(3.W), 0.S(3.W)),
        VecInit(1.S(3.W), 0.S(3.W), 2.S(3.W), 1.S(3.W)), VecInit(0.S(3.W), 1.S(3.W), 2.S(3.W), 1.S(3.W)),

        VecInit(-2.S(3.W), 0.S(3.W), 2.S(3.W), 0.S(3.W)), VecInit(-1.S(3.W), 1.S(3.W), 2.S(3.W), 0.S(3.W)),
        VecInit(-1.S(3.W), 0.S(3.W), 2.S(3.W), 1.S(3.W)), VecInit(-2.S(3.W), 1.S(3.W), 2.S(3.W), 1.S(3.W)),

        VecInit(0.S(3.W), -2.S(3.W), 2.S(3.W), 0.S(3.W)), VecInit(1.S(3.W), -1.S(3.W), 2.S(3.W), 0.S(3.W)),
        VecInit(1.S(3.W), -2.S(3.W), 2.S(3.W), 1.S(3.W)), VecInit(0.S(3.W), -1.S(3.W), 2.S(3.W), 1.S(3.W)),

        VecInit(-2.S(3.W), -2.S(3.W), 2.S(3.W), 0.S(3.W)), VecInit(-1.S(3.W), -1.S(3.W), 2.S(3.W), 0.S(3.W)),
        VecInit(-1.S(3.W), -2.S(3.W), 2.S(3.W), 1.S(3.W)), VecInit(-2.S(3.W), -1.S(3.W), 2.S(3.W), 1.S(3.W)),

        VecInit(0.S(3.W), 0.S(3.W), 2.S(3.W), -2.S(3.W)), VecInit(1.S(3.W), 1.S(3.W), 2.S(3.W), -2.S(3.W)),
        VecInit(1.S(3.W), 0.S(3.W), 2.S(3.W), -1.S(3.W)), VecInit(0.S(3.W), 1.S(3.W), 2.S(3.W), -1.S(3.W)),

        VecInit(-2.S(3.W), 0.S(3.W), 2.S(3.W), -2.S(3.W)), VecInit(-1.S(3.W), 1.S(3.W), 2.S(3.W), -2.S(3.W)),
        VecInit(-1.S(3.W), 0.S(3.W), 2.S(3.W), -1.S(3.W)), VecInit(-2.S(3.W), 1.S(3.W), 2.S(3.W), -1.S(3.W)),

        VecInit(0.S(3.W), -2.S(3.W), 2.S(3.W), -2.S(3.W)), VecInit(1.S(3.W), -1.S(3.W), 2.S(3.W), -2.S(3.W)),
        VecInit(1.S(3.W), -2.S(3.W), 2.S(3.W), -1.S(3.W)), VecInit(0.S(3.W), -1.S(3.W), 2.S(3.W), -1.S(3.W)),

        VecInit(-2.S(3.W), -2.S(3.W), 2.S(3.W), -2.S(3.W)), VecInit(-1.S(3.W), -1.S(3.W), 2.S(3.W), -2.S(3.W)),
        VecInit(-1.S(3.W), -2.S(3.W), 2.S(3.W), -1.S(3.W)), VecInit(-2.S(3.W), -1.S(3.W), 2.S(3.W), -1.S(3.W)),

        VecInit(0.S(3.W), 2.S(3.W), 0.S(3.W), 0.S(3.W)), VecInit(0.S(3.W), 2.S(3.W), 1.S(3.W), 1.S(3.W)),
        VecInit(1.S(3.W), 2.S(3.W), 0.S(3.W), 1.S(3.W)), VecInit(1.S(3.W), 2.S(3.W), 1.S(3.W), 0.S(3.W)),

        VecInit(-2.S(3.W), 2.S(3.W), 0.S(3.W), 0.S(3.W)), VecInit(-2.S(3.W), 2.S(3.W), 1.S(3.W), 1.S(3.W)),
        VecInit(-1.S(3.W), 2.S(3.W), 0.S(3.W), 1.S(3.W)), VecInit(-1.S(3.W), 2.S(3.W), 1.S(3.W), 0.S(3.W)),

        VecInit(0.S(3.W), 2.S(3.W), -2.S(3.W), 0.S(3.W)), VecInit(0.S(3.W), 2.S(3.W), -1.S(3.W), 1.S(3.W)),
        VecInit(1.S(3.W), 2.S(3.W), -2.S(3.W), 1.S(3.W)), VecInit(1.S(3.W), 2.S(3.W), -1.S(3.W), 0.S(3.W)),

        VecInit(-2.S(3.W), 2.S(3.W), -2.S(3.W), 0.S(3.W)), VecInit(-2.S(3.W), 2.S(3.W), -1.S(3.W), 1.S(3.W)),
        VecInit(-1.S(3.W), 2.S(3.W), -2.S(3.W), 1.S(3.W)), VecInit(-1.S(3.W), 2.S(3.W), -1.S(3.W), 0.S(3.W)),

        VecInit(0.S(3.W), 2.S(3.W), 0.S(3.W), -2.S(3.W)), VecInit(0.S(3.W), 2.S(3.W), 1.S(3.W), -1.S(3.W)),
        VecInit(1.S(3.W), 2.S(3.W), 0.S(3.W), -1.S(3.W)), VecInit(1.S(3.W), 2.S(3.W), 1.S(3.W), -2.S(3.W)),

        VecInit(-2.S(3.W), 2.S(3.W), 0.S(3.W), -2.S(3.W)), VecInit(-2.S(3.W), 2.S(3.W), 1.S(3.W), -1.S(3.W)),
        VecInit(-1.S(3.W), 2.S(3.W), 0.S(3.W), -1.S(3.W)), VecInit(-1.S(3.W), 2.S(3.W), 1.S(3.W), -2.S(3.W)),

        VecInit(0.S(3.W), 2.S(3.W), -2.S(3.W), -2.S(3.W)), VecInit(0.S(3.W), 2.S(3.W), -1.S(3.W), -1.S(3.W)),
        VecInit(1.S(3.W), 2.S(3.W), -2.S(3.W), -1.S(3.W)), VecInit(1.S(3.W), 2.S(3.W), -1.S(3.W), -2.S(3.W)),

        VecInit(-2.S(3.W), 2.S(3.W), -2.S(3.W), -2.S(3.W)), VecInit(-2.S(3.W), 2.S(3.W), -1.S(3.W), -1.S(3.W)),
        VecInit(-1.S(3.W), 2.S(3.W), -2.S(3.W), -1.S(3.W)), VecInit(-1.S(3.W), 2.S(3.W), -1.S(3.W), -2.S(3.W)),

        VecInit(0.S(3.W), 0.S(3.W), 0.S(3.W), 2.S(3.W)), VecInit(1.S(3.W), 1.S(3.W), 0.S(3.W), 2.S(3.W)),
        VecInit(0.S(3.W), 1.S(3.W), 1.S(3.W), 2.S(3.W)), VecInit(1.S(3.W), 0.S(3.W), 1.S(3.W), 2.S(3.W)),

        VecInit(-2.S(3.W), 0.S(3.W), 0.S(3.W), 2.S(3.W)), VecInit(-1.S(3.W), 1.S(3.W), 0.S(3.W), 2.S(3.W)),
        VecInit(-2.S(3.W), 1.S(3.W), 1.S(3.W), 2.S(3.W)), VecInit(-1.S(3.W), 0.S(3.W), 1.S(3.W), 2.S(3.W)),

        VecInit(0.S(3.W), -2.S(3.W), 0.S(3.W), 2.S(3.W)), VecInit(1.S(3.W), -1.S(3.W), 0.S(3.W), 2.S(3.W)),
        VecInit(0.S(3.W), -1.S(3.W), 1.S(3.W), 2.S(3.W)), VecInit(1.S(3.W), -2.S(3.W), 1.S(3.W), 2.S(3.W)),

        VecInit(-2.S(3.W), -2.S(3.W), 0.S(3.W), 2.S(3.W)), VecInit(-1.S(3.W), -1.S(3.W), 0.S(3.W), 2.S(3.W)),
        VecInit(-2.S(3.W), -1.S(3.W), 1.S(3.W), 2.S(3.W)), VecInit(-1.S(3.W), -2.S(3.W), 1.S(3.W), 2.S(3.W)),

        VecInit(0.S(3.W), 0.S(3.W), -2.S(3.W), 2.S(3.W)), VecInit(1.S(3.W), 1.S(3.W), -2.S(3.W), 2.S(3.W)),
        VecInit(0.S(3.W), 1.S(3.W), -1.S(3.W), 2.S(3.W)), VecInit(1.S(3.W), 0.S(3.W), -1.S(3.W), 2.S(3.W)),

        VecInit(-2.S(3.W), 0.S(3.W), -2.S(3.W), 2.S(3.W)), VecInit(-1.S(3.W), 1.S(3.W), -2.S(3.W), 2.S(3.W)),       
        VecInit(-2.S(3.W), 1.S(3.W), -1.S(3.W), 2.S(3.W)), VecInit(-1.S(3.W), 0.S(3.W), -1.S(3.W), 2.S(3.W)),

        VecInit(0.S(3.W), -2.S(3.W), -2.S(3.W), 2.S(3.W)), VecInit(1.S(3.W), -1.S(3.W), -2.S(3.W), 2.S(3.W)),
        VecInit(0.S(3.W), -1.S(3.W), -1.S(3.W), 2.S(3.W)), VecInit(1.S(3.W), -2.S(3.W), -1.S(3.W), 2.S(3.W)),

        VecInit(-2.S(3.W), -2.S(3.W), -2.S(3.W), 2.S(3.W)), VecInit(-1.S(3.W), -1.S(3.W), -2.S(3.W), 2.S(3.W)),
        VecInit(-2.S(3.W), -1.S(3.W), -1.S(3.W), 2.S(3.W)), VecInit(-1.S(3.W), -2.S(3.W), -1.S(3.W), 2.S(3.W))
    )

    def XMT_ERR: Seq[Vec[SInt]] = Seq(
        VecInit(0.S(3.W), 2.S(3.W), 2.S(3.W), 0.S(3.W)), VecInit(1.S(3.W), 1.S(3.W), 2.S(3.W), 2.S(3.W)),
        VecInit(2.S(3.W), 1.S(3.W), 1.S(3.W), 2.S(3.W)), VecInit(2.S(3.W), 1.S(3.W), 2.S(3.W), 1.S(3.W))
    )

    def CSEXTEND_ERR: Seq[Vec[SInt]] = Seq(
        VecInit(-2.S(3.W), 2.S(3.W), 2.S(3.W), -2.S(3.W)), VecInit(-1.S(3.W), -1.S(3.W), 2.S(3.W), 2.S(3.W)),
        VecInit(2.S(3.W), -1.S(3.W), -1.S(3.W), 2.S(3.W)), VecInit(2.S(3.W), -1.S(3.W), 2.S(3.W), -1.S(3.W))
    )

    def CSEXTEND: Seq[Vec[SInt]] = Seq(
        VecInit(2.S(3.W), 0.S(3.W), 0.S(3.W), 2.S(3.W)), VecInit(2.S(3.W), 2.S(3.W), 1.S(3.W), 1.S(3.W)),
        VecInit(1.S(3.W), 2.S(3.W), 2.S(3.W), 1.S(3.W)), VecInit(1.S(3.W), 2.S(3.W), 1.S(3.W), 2.S(3.W))
    )

    def CSRESET: Seq[Vec[SInt]] = Seq(
        VecInit(2.S(3.W), -2.S(3.W), -2.S(3.W), 2.S(3.W)), VecInit(2.S(3.W), 2.S(3.W), -1.S(3.W), -1.S(3.W)),
        VecInit(-1.S(3.W), 2.S(3.W), 2.S(3.W), -1.S(3.W)), VecInit(-1.S(3.W), 2.S(3.W), -1.S(3.W), 2.S(3.W))
    )

    def SSD1: Seq[Vec[SInt]] = Seq(
        VecInit(2.S(3.W), 2.S(3.W), 2.S(3.W), 2.S(3.W))
    )

    def SSD2: Seq[Vec[SInt]] = Seq(
        VecInit(2.S(3.W), 2.S(3.W), 2.S(3.W), -2.S(3.W))
    )

    def ESD1: Seq[Vec[SInt]] = Seq(
        VecInit(2.S(3.W), 2.S(3.W), 2.S(3.W), -2.S(3.W))
    )

    def ESD2: Seq[Vec[SInt]] = Seq(
        VecInit(2.S(3.W), 2.S(3.W), 2.S(3.W), -2.S(3.W))
    )

    def ESD2_EXT_0: Seq[Vec[SInt]] = Seq(
        VecInit(2.S(3.W), 2.S(3.W), 2.S(3.W), -2.S(3.W))
    )

    def ESD2_EXT_1: Seq[Vec[SInt]] = Seq(
        VecInit(2.S(3.W), 2.S(3.W), -2.S(3.W), 2.S(3.W))
    )

    def ESD2_EXT_2: Seq[Vec[SInt]] = Seq(
        VecInit(2.S(3.W), -2.S(3.W), 2.S(3.W), 2.S(3.W))
    )

    def ESD_EXT_ERR: Seq[Vec[SInt]] = Seq(
        VecInit(-2.S(3.W), 2.S(3.W), 2.S(3.W), 2.S(3.W))
    )

    def IDLE: Seq[Vec[SInt]] = Seq(
        VecInit(0.S(3.W), 0.S(3.W), 0.S(3.W), 0.S(3.W)), VecInit(-2.S(3.W), 0.S(3.W), 0.S(3.W), 0.S(3.W)),
        VecInit(0.S(3.W), -2.S(3.W), 0.S(3.W), 0.S(3.W)), VecInit(-2.S(3.W), -2.S(3.W), 0.S(3.W), 0.S(3.W)),
        VecInit(0.S(3.W), 0.S(3.W), -2.S(3.W), 0.S(3.W)), VecInit(-2.S(3.W), 0.S(3.W), -2.S(3.W), 0.S(3.W)),
        VecInit(0.S(3.W), -2.S(3.W), -2.S(3.W), 0.S(3.W)), VecInit(-2.S(3.W), -2.S(3.W), -2.S(3.W), 0.S(3.W)),
        VecInit(0.S(3.W), 0.S(3.W), 0.S(3.W), -2.S(3.W)), VecInit(-2.S(3.W), 0.S(3.W), 0.S(3.W), -2.S(3.W)),
        VecInit(0.S(3.W), -2.S(3.W), 0.S(3.W), -2.S(3.W)), VecInit(-2.S(3.W), -2.S(3.W), 0.S(3.W), -2.S(3.W)),
        VecInit(0.S(3.W), 0.S(3.W), -2.S(3.W), -2.S(3.W)), VecInit(-2.S(3.W), 0.S(3.W), -2.S(3.W), -2.S(3.W)),
        VecInit(0.S(3.W), -2.S(3.W), -2.S(3.W), -2.S(3.W)), VecInit(-2.S(3.W), -2.S(3.W), -2.S(3.W), -2.S(3.W)),
    )

    def vecEqual(a: Vec[SInt], b: Vec[SInt]): Bool = {
        a.zip(b).map { case (x, y) => x === y }.reduce(_ && _)
    }

    def isDATA(symb: Vec[SInt]): Bool = {
        DATA.map(refVec => vecEqual(symb, refVec)).reduce(_ || _)    
    }

    def isXMT_ERR(symb: Vec[SInt]): Bool = {
        XMT_ERR.map(refVec => vecEqual(symb, refVec)).reduce(_ || _)    
    }

    def isCSEXTEND_ERR(symb: Vec[SInt]): Bool = {
        CSEXTEND_ERR.map(refVec => vecEqual(symb, refVec)).reduce(_ || _)    
    }

    def isCSEXTEND(symb: Vec[SInt]): Bool = {
        CSEXTEND.map(refVec => vecEqual(symb, refVec)).reduce(_ || _)    
    }

    def isCSRESET(symb: Vec[SInt]): Bool = {
        CSRESET.map(refVec => vecEqual(symb, refVec)).reduce(_ || _)    
    }

    def isSSD1(symb: Vec[SInt]): Bool = {
        SSD1.map(refVec => vecEqual(symb, refVec)).reduce(_ || _)    
    }

    def isSSD2(symb: Vec[SInt]): Bool = {
        SSD2.map(refVec => vecEqual(symb, refVec)).reduce(_ || _)    
    }

    def isESD1(symb: Vec[SInt]): Bool = {
        ESD1.map(refVec => vecEqual(symb, refVec)).reduce(_ || _)    
    }

    def isESD2(symb: Vec[SInt]): Bool = {
        ESD2.map(refVec => vecEqual(symb, refVec)).reduce(_ || _)    
    }

    def isESD2_EXT_0(symb: Vec[SInt]): Bool = {
        ESD2_EXT_0.map(refVec => vecEqual(symb, refVec)).reduce(_ || _)    
    }

    def isESD2_EXT_1(symb: Vec[SInt]): Bool = {
        ESD2_EXT_1.map(refVec => vecEqual(symb, refVec)).reduce(_ || _)    
    }

    def isESD2_EXT_2(symb: Vec[SInt]): Bool = {
        ESD2_EXT_2.map(refVec => vecEqual(symb, refVec)).reduce(_ || _)    
    }

    def isESD_EXT_ERR(symb: Vec[SInt]): Bool = {
        ESD_EXT_ERR.map(refVec => vecEqual(symb, refVec)).reduce(_ || _)    
    }

    def isIDLE(symb: Vec[SInt]): Bool = {
        IDLE.map(refVec => vecEqual(symb, refVec)).reduce(_ || _)    
    }
}


