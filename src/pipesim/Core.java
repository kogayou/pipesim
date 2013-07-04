/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pipesim;

import java.util.Arrays;

/**
 *
 * @author kogayou
 */
public class Core {
    
    final static long maxTmp=0x7fffffff;
    final static long maxNum=maxTmp*2+1;
    final static int maxMemory=0x00000fff;
    static int memory[]=new int[maxMemory+1];
    static int memorybak[]=new int[maxMemory+1];
    static boolean modify[]=new boolean[maxMemory+1];
    
    static boolean isOver;
    static String message;
    
    static int cycle,instruction;
    static boolean addinstuction;
    
    static int PC;
    static boolean ZF,SF,OF;
    static long r[]=new long[16];
    
    final static int IHALT=0x0;
    final static int INOP=0x1;
    final static int IRRMOVL=0x2;
    final static int IIRMOVL=0x3;
    final static int IRMMOVL=0x4;
    final static int IMRMOVL=0x5;
    final static int IOPL=0x6;
    final static int IJXX=0x7;
    final static int ICALL=0x8;
    final static int IRET=0x9;
    final static int IPUSHL=0xa;
    final static int IPOPL=0xb;
    final static int IIADDL=0xc;
    final static int ILEAVE=0xd;    
    
    final static int FNONE=0x0;
    
    final static int REAX=0x0;
    final static int RECX=0x1;
    final static int REDX=0x2;
    final static int REBX=0x3;
    final static int RESP=0x4;
    final static int REBP=0x5;
    final static int RESI=0x6;
    final static int REDI=0x7;
    final static int RNONE=0xf;
    
    final static int ALUADD=0x0;
    final static int ALUSUB=0x1;
    final static int ALUAND=0x2;
    final static int ALUXOR=0x3;
    final static int ALUOR=0x4;
    
    final static int SAOK=0x0;
    final static int SSTA=0x1;
    final static int SBUB=0x2;
    
    final static int ANONE=-1;
    
    static int F_addr;
    static int f_addr;
    static int F_stat;
    static int F_predPC;
    static int f_predPC;
    static int f_icode;
    static int f_ifun;
    static long f_valC;
    static int f_valP;
    static int f_pc;
    static int f_rA;
    static int f_rB;
    
    static int D_addr;
    static int d_addr;
    static int D_stat;
    static int D_icode;
    static int D_ifun;
    static int D_rA;
    static int D_rB;
    static long D_valC;
    static int D_valP;
    static int d_icode;
    static int d_ifun;
    static int d_rA;
    static int d_rB;
    static long d_valC;
    static int d_valP;
    static int d_srcA;
    static int d_srcB;
    static long d_rvalA;
    static long d_rvalB;
    static int d_dstE;
    static int d_dstM;
    static long d_valA;
    static long d_valB;

    static int E_addr;
    static int e_addr;
    static int E_stat;
    static int E_icode;
    static int E_ifun;
    static long E_valC;
    static long E_valA;
    static long E_valB;
    static int E_dstE;
    static int E_dstM;
    static int E_srcA;
    static int E_srcB;
    static int e_icode;
    static int e_ifun;
    static long e_valC;
    static long e_valA;
    static long e_valB;
    static int e_dstE;
    static int e_dstM;
    static int e_srcA;
    static int e_srcB;
    static long aluA,aluB;
    static long e_valE;
    static boolean e_Cnd;

    static int M_addr;
    static int m_addr;
    static int M_stat;
    static int M_icode;
    static boolean M_Cnd;
    static long M_valE;
    static long M_valA;
    static int M_dstE;
    static int M_dstM;
    static int m_icode;
    static long m_valE;
    static long m_valM;
    static int m_dstE;
    static int m_dstM;
    static int mem_addr;

    static int W_addr;
    static int W_stat;
    static int W_icode;
    static long W_valE;
    static long W_valM;
    static int W_dstE;
    static int W_dstM;
    
    static int f_pc()
    {
        if(M_icode==IJXX&&!M_Cnd) return (int)M_valA;
        if(W_icode==IRET) return (int)W_valM;
        return F_predPC;
    }

    static int f_icode()
    {
        return memory[PC]>>4;
    }
    
    static int f_ifun()
    {
        return memory[PC]&0xf;
    }
    
    static boolean instr_valid()   
    {   
        if(f_icode>=IHALT&&f_icode<=ILEAVE)
            return true;
        return false;
    }
    
    static boolean need_regids()
    {   
        if(f_icode==IRRMOVL||f_icode==IOPL||f_icode==IPUSHL||f_icode==IPOPL||f_icode==IIRMOVL||f_icode==IRMMOVL||f_icode==IMRMOVL||f_icode==IIADDL)
            return true;
        return false;
    }
    
    static boolean need_valC()   
    {   
        if(f_icode==IIRMOVL||f_icode==IRMMOVL||f_icode==IMRMOVL||f_icode==IJXX||f_icode==ICALL||f_icode==IIADDL)
            return true;
        return false;
    }
    
    static long f_valC()   
    {   
        if (!need_valC())
            return 0;
        if(!need_regids())   
            return (long)memory[PC+1]+((long)memory[PC+2]<<8)+((long)memory[PC+3]<<16)+((long)memory[PC+4]<<24);
        return (long)memory[PC+2]+((long)memory[PC+3]<<8)+((long)memory[PC+4]<<16)+((long)memory[PC+5]<<24);
    }
    
    static int f_valP()   
    {   
        if(!need_regids()&&!need_valC())return PC+1;
        if(need_regids()&&!need_valC())return PC+2;
        if(!need_regids()&&need_valC())return PC+5;
        return PC+6;
    }
    
    static int f_predPC()   
    {   
        if(f_icode==IJXX||f_icode==ICALL)return (int)f_valC;
        return f_valP;
    }
    
    static int d_srcA()   
    {   
        if(D_icode==IRRMOVL||D_icode==IRMMOVL||D_icode==IOPL||D_icode==IPUSHL)
            return D_rA;
        if(D_icode==IPOPL||D_icode==IRET)
            return RESP;
        if(D_icode==ILEAVE)
            return REBP;
        return RNONE;
    }

    static int d_srcB()   
    {   
        if(D_icode==IOPL||D_icode==IRMMOVL||D_icode==IMRMOVL||D_icode==IIADDL)
            return D_rB;
        if(D_icode==IPUSHL||D_icode==IPOPL||D_icode==ICALL||D_icode==IRET)
            return RESP;
        return RNONE;
    }   
   
    static int d_dstE()   
    {   
        if(D_icode==IRRMOVL||D_icode==IIRMOVL||D_icode==IOPL||D_icode==IIADDL)
            return D_rB;
        if(D_icode==IPUSHL||D_icode==IPOPL||D_icode==ICALL||D_icode==IRET||D_icode==ILEAVE)
            return RESP;
        return RNONE;
    }
    
    static int d_dstM()   
    {   
        if(D_icode==IMRMOVL||D_icode==IPOPL)
            return D_rA;
        if(D_icode==ILEAVE)
            return REBP;
        return RNONE;
    }
    
    static long d_rvalA()   
    {   
        switch(d_srcA)   
        {   
            case 0:return r[REAX];
            case 1:return r[RECX];
            case 2:return r[REDX];
            case 3:return r[REBX];
            case 4:return r[RESP];
            case 5:return r[REBP];
            case 6:return r[RESI];
            case 7:return r[REDI];
            default:return r[RNONE];
        }   
    }
    
    static long d_rvalB()   
    {   
        switch(d_srcB)   
        {   
            case 0:return r[REAX];
            case 1:return r[RECX];
            case 2:return r[REDX];
            case 3:return r[REBX];
            case 4:return r[RESP];
            case 5:return r[REBP];
            case 6:return r[RESI];
            case 7:return r[REDI];
            default:return r[RNONE];
        }
    }
    
    static long d_valA()   
    {   
        if(D_icode==ICALL||D_icode==IJXX)
            return D_valP;
        if(d_srcA==E_dstE)
            return e_valE;
        if(d_srcA==M_dstM)
            return m_valM;
        if(d_srcA==M_dstE)
            return M_valE;
        if(d_srcA==W_dstM)
            return W_valM;
        if(d_srcA==W_dstE)
            return W_valE;
        return d_rvalA;
    }
    
    static long d_valB()   
    {   
        if(d_srcB==E_dstE)
            return e_valE;
        if(d_srcB==M_dstM)
            return m_valM;
        if(d_srcB==M_dstE)
            return M_valE;
        if(d_srcB==W_dstM)
            return W_valM;
        if(d_srcB==W_dstE)
            return W_valE;
        return d_rvalB;
    }
    
    static long aluA()   
    {   
        if(E_icode==IRRMOVL||E_icode==IOPL||E_icode==ILEAVE)
            return E_valA;
        if(E_icode==IIRMOVL||E_icode==IRMMOVL||E_icode==IMRMOVL||E_icode==IIADDL)
            return E_valC;
        if(E_icode==ICALL||E_icode==IPUSHL)
            return -4;
        if(E_icode==IRET||E_icode==IPOPL)
            return 4;
        return 0;
    }
    
    static long aluB()   
    {   
        if(E_icode==IRMMOVL||E_icode==IMRMOVL||E_icode==IOPL||E_icode==ICALL||E_icode==IPUSHL||E_icode==IRET||E_icode==IPOPL||E_icode==IIADDL)
            return E_valB;
        if(E_icode==ILEAVE)
            return 4;
        return 0;
    }

    static int alufun()   
    {   
        if(E_icode==IOPL)
            return E_ifun;
        return ALUADD;
    }

    static long e_valE(long aluA,long aluB)   
    {
        long res=0;
        switch(alufun())   
        {   
            case ALUADD:res=aluB+aluA;break;
            case ALUSUB:res=aluB-aluA;break;
            case ALUAND:res=aluB&aluA;break;
            case ALUXOR:res=aluB^aluA;break;
            case ALUOR:res=aluB|aluA;break;    
        }
        return res&maxNum;
    }
    
    static void set_cc()
    {   
        if(E_icode!=IOPL&&E_icode!=IIADDL)
            return;
       // m_stat in { SADR, SINS, SHLT } || W_stat in { SADR, SINS, SHLT } return false;
        ZF=(e_valE==0);
        SF=(e_valE>maxTmp);//(e_valE<0);
        long aluBTmp=aluB;
        if (E_ifun==ALUSUB)
            aluBTmp=(-aluB)&maxNum;
        OF=(e_valE>0&&e_valE<=maxTmp&&aluA>maxTmp&&aluBTmp>maxTmp||e_valE>maxTmp&&aluA>0&&aluA<=maxTmp&&aluBTmp>0&&aluBTmp<=maxTmp);//(e_valE>0&&aluA<0&&aluB<0||e_valE<0&&aluA>0&&aluB>0);
    }   

    static boolean e_Cnd()   
    {   
        if(E_icode!=IJXX)
            return false;
        switch(E_ifun)   
        {      
            case 1:return (SF^OF)||ZF;
            case 2:return SF^OF;
            case 3:return ZF;
            case 4:return !ZF;
            case 5:return !(SF^OF);
            case 6:return !(SF^OF)&&!ZF;
            default:return true;
        } 
    }
    
    static int e_dstE()
    {
        if (E_icode==IRRMOVL||!e_Cnd)
            return RNONE;
        return E_dstE;
    }
    
    static int mem_addr()   
    {   
        if(M_icode==IRMMOVL||M_icode==IPUSHL||M_icode==ICALL||M_icode==IMRMOVL)
            return (int)M_valE;
        if(M_icode==IPOPL||M_icode==IRET||M_icode==ILEAVE)
            return (int)M_valA;
        return 0;
    }
    
    static boolean mem_read()   
    {   
        if(M_icode==IMRMOVL||M_icode==IPOPL||M_icode==IRET||M_icode==ILEAVE)
            return true;
        return false;
    }
    
    static boolean mem_write()   
    {   
        if(M_icode==IRMMOVL||M_icode==IPUSHL||M_icode==ICALL)
            return true;
        return false;
    }
    
    static boolean F_stall()   
    {   
        if((E_icode==IMRMOVL||E_icode==IPOPL)&&(E_dstM==d_srcA||E_dstM==d_srcB))
            return true;
        if(IRET==D_icode||IRET==E_icode||IRET==M_icode)
            return true;
        return false;
    }
    
    static boolean D_stall()   
    {   
        if((E_icode==IMRMOVL||E_icode==IPOPL)&&(E_dstM==d_srcA||E_dstM==d_srcB))
            return true;
        return false;
    }
    
    static boolean D_bubble()   
    {   
        if(E_icode==IJXX&&!e_Cnd)
            return true;
        if(IRET==D_icode||IRET==E_icode||IRET==M_icode)
            return true;
        return false;
    }
    
    static boolean E_bubble()   
    {   
        if(E_icode==IJXX&&!e_Cnd)
            return true;
        if((E_icode==IMRMOVL||E_icode==IPOPL)&&(E_dstM==d_srcA||E_dstM==d_srcB))
            return true;
        return false;
    }
    
    static void Fetch_set()
    {
        if(F_stall())
        {
            F_stat=SSTA;
            return;
        }   
        F_stat=SAOK;
        F_predPC=f_predPC;
        F_addr=f_predPC;
    }
    
    static void Fetch()   
    {
        f_pc=f_pc();
        PC=f_pc;
        f_addr=f_pc;
        f_icode=f_icode();
        f_ifun=f_ifun();
        if(!instr_valid())
        {
            isOver=true;
            message="出现非法指令";
        }
        if(need_regids())   
        {   
            f_rA=memory[PC+1]>>4;
            f_rB=memory[PC+1]&0xf;
        }
        else
        {
            f_rA=RNONE;
            f_rB=RNONE;            
        }
        f_valC=f_valC();
        f_valP=f_valP();
        f_predPC=f_predPC();
    }
    
    static void Decode_set()
    {
        if(D_stall())
        {
            D_stat=SSTA;
            return;
        }
        if (!D_bubble())
        {
            D_addr=f_addr;
            D_stat=SAOK;
            D_icode=f_icode;
            D_ifun=f_ifun;
            D_rA=f_rA;
            D_rB=f_rB;
            D_valC=f_valC;
            D_valP=f_valP;            
        }
        else
        {
            D_addr=ANONE;
            D_stat=SBUB;
            D_icode=INOP;
            D_ifun=FNONE;
            D_rA=RNONE;
            D_rB=RNONE;
            D_valC=0;
            D_valP=0;      
        }
    }
    
    static void Decode()   
    {   
        d_addr=D_addr;
        d_icode=D_icode;
        d_ifun=D_ifun;
        d_rA=D_rA;
        d_rB=D_rB;
        d_valC=D_valC;
        d_valP=D_valP;
        d_srcA=d_srcA();
        d_srcB=d_srcB();
        d_dstE=d_dstE();
        d_dstM=d_dstM();
        d_rvalA=d_rvalA();
        d_rvalB=d_rvalB();
        d_valA=d_valA();
        d_valB=d_valB();
        
    }   
   
    static void Excute_set()
    {
        if(!E_bubble())   
        {   
            E_addr=d_addr;
            E_stat=SAOK;
            E_icode=d_icode;
            E_ifun=d_ifun;
            E_valC=d_valC;
            E_srcA=d_srcA;
            E_srcB=d_srcB;
            E_dstE=d_dstE;
            E_dstM=d_dstM;
            E_valA=d_valA;
            E_valB=d_valB;
        }
        else
        {
            E_addr=ANONE;
            E_stat=SBUB;
            E_icode=INOP;
            E_ifun=FNONE;
            E_valC=0;
            E_srcA=RNONE;
            E_srcB=RNONE;
            E_dstE=RNONE;
            E_dstM=RNONE;
            E_valA=0;
            E_valB=0;
        }
    }
    
    static void Excute()   
    {   
        e_addr=E_addr;
        e_icode=E_icode;
        e_valC=E_valC;
        e_valA=E_valA;
        e_valB=E_valB;
        e_dstE=E_dstE;
        e_dstM=E_dstM;
        aluA=aluA();
        aluB=aluB();
        e_valE=e_valE(aluA,aluB);
        set_cc();  
        e_Cnd=e_Cnd();
    }
    
    static void Memory_set()
    {
        M_addr=e_addr;
        M_stat=SAOK;
        M_icode=e_icode;
        M_Cnd=e_Cnd;
        M_valE=e_valE;
        M_valA=e_valA;
        M_dstE=e_dstE;
        M_dstM=e_dstM;
    }
    
    static void Memory()   
    {
        m_addr=M_addr;
        m_icode=M_icode;
        m_valE=M_valE;
        m_dstE=M_dstE;
        m_dstM=M_dstM;
        mem_addr=mem_addr();
        if(mem_addr<0||mem_addr+3>maxMemory)
        {
            isOver=true;
            message="出现非法地址";
            return;
        }
        if(mem_read())   
            m_valM=(long)memory[mem_addr]+((long)memory[mem_addr+1]<<8)+((long)memory[mem_addr+2]<<16)+((long)memory[mem_addr+3]<<24);
        if(mem_write())   
        {      
            memory[mem_addr]=(int)M_valA&0xff;
            memory[mem_addr+1]=((int)M_valA>>8)&0xff;
            memory[mem_addr+2]=((int)M_valA>>16)&0xff;
            memory[mem_addr+3]=((int)M_valA>>24)&0xff;
        }
    }
   
    static void Writeback_set()
    {
        W_addr=m_addr;
        W_stat=SAOK;
        W_icode=m_icode;
        W_valE=m_valE;
        W_valM=m_valM;
        W_dstE=m_dstE;
        W_dstM=m_dstM;        
    }
    
    static void Writeback()
    {
        r[W_dstE]=W_valE;
        r[W_dstM]=W_valM;
    }
    
    static void reset()
    {
        System.arraycopy(memorybak,0,memory,0,memory.length);
        cycle=0;
        instruction=0;
        addinstuction=false;
        PC=0;
        ZF=SF=OF=false;
        Arrays.fill(r,0);
        F_addr=ANONE;
        F_stat=SAOK;
        F_predPC=0;
        f_addr=ANONE;
        f_predPC=0;
        f_icode=INOP;
        f_ifun=FNONE;
        f_valC=0;
        f_valP=0;
        f_pc=0;
        f_rA=RNONE;
        f_rB=RNONE;
        D_addr=ANONE;
        D_stat=SAOK;
        D_icode=INOP;
        D_ifun=FNONE;
        D_rA=RNONE;
        D_rB=RNONE;
        D_valC=0;
        D_valP=0;
        d_addr=ANONE;
        d_icode=INOP;
        d_ifun=FNONE;
        d_rA=RNONE;
        d_rB=RNONE;
        d_valC=0;
        d_valP=0;
        d_srcA=RNONE;
        d_srcB=RNONE;
        d_rvalA=0;
        d_rvalB=0;
        d_dstE=RNONE;
        d_dstM=RNONE;
        d_valA=0;
        d_valB=0;
        E_addr=ANONE;
        E_stat=SAOK;
        E_icode=INOP;
        E_ifun=FNONE;
        E_valC=0;
        E_valA=0;
        E_valB=0;
        E_dstE=RNONE;
        E_dstM=RNONE;
        E_srcA=RNONE;
        E_srcB=RNONE;
        e_addr=ANONE;
        e_icode=INOP;
        e_ifun=FNONE;
        e_valC=0;
        e_valA=0;
        e_valB=0;
        e_dstE=RNONE;
        e_dstM=RNONE;
        e_srcA=RNONE;
        e_srcB=RNONE;
        aluA=0;
        aluB=0;
        e_valE=0;
        e_Cnd=false;
        M_addr=ANONE;
        M_stat=SAOK;
        M_icode=INOP;
        M_Cnd=false;
        M_valE=0;
        M_valA=0;
        M_dstE=RNONE;
        M_dstM=RNONE;
        m_addr=ANONE;
        m_icode=INOP;
        m_valE=0;
        m_valM=0;
        m_dstE=RNONE;
        m_dstM=RNONE;
        mem_addr=0;
        W_addr=ANONE;
        W_stat=SAOK;
        W_icode=INOP;
        W_valE=0;
        W_valM=0;
        W_dstE=RNONE;
        W_dstM=RNONE;
    }
    
    static void step()
    {
        if (addinstuction)
            instruction++;
        Writeback();
        Memory();
        if (isOver)
            return;
        Excute();
        Decode();
        Fetch();
        if (isOver)
            return;
        if (W_icode==IHALT)
        {
            isOver=true;
            message="程序正常结束";
            return;
        }
        Fetch_set();
        Decode_set();
        Excute_set();
        Memory_set();
        Writeback_set();
        cycle++;
    }
} 
    

