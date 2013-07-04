/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pipesim;

import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

/**
 *
 * @author kogayou
 */
public class Assembler {
    
    final static int maxLine=1000;

    static File inputFile;
    static String errorMessage;
    static String binaryCode[]=new String[maxLine];
    static String originCode[]=new String[maxLine];
    static long codeAddress[]=new long[maxLine];
    static int lineNum;
    static String labelName[]=new String[maxLine];
    static long labelAddress[]=new long[maxLine];
    static String labelCall[]=new String[maxLine];
    static int labelLine[]=new int[maxLine];
    static int labelOffset[]=new int[maxLine];
    static int labelNum,labelNum2;
    static boolean error;

    
    static boolean assemble() throws Exception
    {
        FileReader fileReader=new FileReader(inputFile);
        Scanner cin=new Scanner(fileReader);
        lineNum=0;
        labelNum=0;
        labelNum2=0;
        while (cin.hasNextLine())
        {
            lineNum++;
            if (lineNum>=maxLine)
            {
                errorMessage="代码长度超出上限"+maxLine+"行";
                return false;
            }
            originCode[lineNum]=cin.nextLine();
        }
        codeAddress[0]=0;
        binaryCode[0]="";
        for (int i=1;i<=lineNum;i++)
        {
            binaryCode[i]="";
            codeAddress[i]=codeAddress[i-1]+binaryCode[i-1].length()/2;
            String curLine=originCode[i].trim();
            //去注释
            if (curLine.startsWith("/*"))   continue;
            if (curLine.indexOf("#")>=0)
                curLine=curLine.substring(0,curLine.indexOf("#"));
            if (curLine.indexOf("//")>=0)
                curLine=curLine.substring(0,curLine.indexOf("//"));
            curLine=curLine.trim();
            originCode[i]=curLine;
            if (curLine.isEmpty()) continue;
            //处理标记
            while (curLine.indexOf(":")>=0)
            {
                labelNum++;
                labelName[labelNum]=curLine.substring(0,curLine.indexOf(":"));
                labelAddress[labelNum]=codeAddress[i];
                if (labelName[labelNum].isEmpty())
                {
                    errorMessage="第"+i+"行缺少标记";
                    return false;
                }
                for (int j=1;j<labelNum;j++)
                    if (labelName[j].equals(labelName[labelNum]))
                    {
                        errorMessage="第"+i+"行出现重名标记";
                        return false; 
                    }
                curLine=curLine.substring(curLine.indexOf(":")+1).trim();
            }
            if (curLine.isEmpty()) continue;
            if (curLine.startsWith("."))
            {
                if (curLine.startsWith(".pos ")||curLine.startsWith(".align ")||curLine.startsWith(".long "))
                {
                    //处理.pos
                    if (curLine.startsWith(".pos "))
                    {
                        long tmp=toLong(curLine.substring(4).trim());
                        if (error)
                        {
                            errorMessage="第"+i+"行出现非法数值";
                            return false;     
                        }
                        codeAddress[i]=tmp;
                    }                               
                    //处理.align
                    if (curLine.startsWith(".align "))
                    {
                        long tmp=toLong(curLine.substring(6).trim());
                        if (error||(tmp!=1&&tmp!=2&&tmp!=4&&tmp!=8&&tmp!=16))
                        {
                            errorMessage="第"+i+"行出现非法数值";
                            return false;     
                        }
                        while (codeAddress[i]%tmp!=0)
                            codeAddress[i]++;
                    }
                    //处理.long
                    if (curLine.startsWith(".long "))
                    {
                        curLine=curLine.substring(5).trim();
                        if (curLine.charAt(0)>='0'&&curLine.charAt(0)<='9'||curLine.charAt(0)=='-')
                        {
                            long tmp=toLong(curLine);
                            if (error)
                            {
                                errorMessage="第"+i+"行出现非法数值";
                                return false;     
                            }
                            binaryCode[i]+=toLittleEndian(tmp);     
                        }
                        else
                        {
                            labelNum2++;
                            labelCall[labelNum2]=curLine;
                            labelLine[labelNum2]=i;
                            labelOffset[labelNum2]=0;
                            binaryCode[i]+="00000000";
                        }
                    }                    
                }
                else
                {
                    errorMessage="第"+i+"行出现非法汇编器命令";
                    return false;
                }
                continue;
            }
            //处理Y86语句
            if (curLine.equals("halt"))
            {
                binaryCode[i]+="00";
                continue;
            }
            if (curLine.equals("nop"))
            {
                binaryCode[i]+="10";
                continue;
            }
            if (curLine.equals("ret"))
            {
                binaryCode[i]+="90";
                continue;
            }
            if (curLine.equals("leave"))
            {
                binaryCode[i]+="d0";
                continue;
            }
            if (curLine.startsWith("rrmovl")||curLine.startsWith("addl")||curLine.startsWith("subl")||curLine.startsWith("andl")||curLine.startsWith("xorl")||curLine.startsWith("orl"))
            {
                if (curLine.startsWith("rrmovl"))
                {
                    binaryCode[i]+="20";
                    curLine=curLine.substring(6).trim();
                }
                if (curLine.startsWith("addl"))
                {
                    binaryCode[i]+="60";
                    curLine=curLine.substring(4).trim();
                }
                if (curLine.startsWith("subl"))
                {
                    binaryCode[i]+="61";
                    curLine=curLine.substring(4).trim();
                }
                if (curLine.startsWith("andl"))
                {
                    binaryCode[i]+="62";
                    curLine=curLine.substring(4).trim();
                }
                if (curLine.startsWith("xorl"))
                {
                    binaryCode[i]+="63";
                    curLine=curLine.substring(4).trim();
                }
                if (curLine.startsWith("orl"))
                {
                    binaryCode[i]+="64";
                    curLine=curLine.substring(3).trim();
                }
                if (curLine.indexOf(",")<0)
                {
                    errorMessage="第"+i+"行出现非法指令";
                    return false;
                }
                String ra=getRegister(curLine.substring(0,curLine.indexOf(","))).trim();
                String rb=getRegister(curLine.substring(curLine.indexOf(",")+1)).trim();
                if (ra.isEmpty()||rb.isEmpty())
                {
                    errorMessage="第"+i+"行出现非法寄存器";
                    return false;
                }
                binaryCode[i]+=ra+rb;
                continue;
            }
            if (curLine.startsWith("irmovl")||curLine.startsWith("iaddl"))
            {
                if (curLine.startsWith("irmovl"))
                {
                    binaryCode[i]+="30f";
                    curLine=curLine.substring(6).trim();
                }
                else
                {
                    binaryCode[i]+="c0f";
                    curLine=curLine.substring(5).trim();                    
                }
                if (curLine.indexOf(",")<0)
                {
                    errorMessage="第"+i+"行出现非法指令";
                    return false;
                }
                String rb=getRegister(curLine.substring(curLine.indexOf(",")+1));
                if (rb.isEmpty())
                {
                    errorMessage="第"+i+"行出现非法寄存器";
                    return false;
                }
                binaryCode[i]+=rb;
                if (curLine.charAt(0)=='$')
                {
                    String num=curLine.substring(1,curLine.indexOf(","));
                    long tmp=toLong(num);
                    if (error)
                    {
                        errorMessage="第"+i+"行出现非法数值";
                        return false; 
                    }
                    num=toLittleEndian(tmp);
                    binaryCode[i]+=num;
                }
                else
                {
                    labelNum2++;
                    labelCall[labelNum2]=curLine.substring(0,curLine.indexOf(","));
                    labelLine[labelNum2]=i;
                    labelOffset[labelNum2]=4;
                    binaryCode[i]+="00000000";
                }
                continue;
            }
            if (curLine.startsWith("rmmovl")||curLine.startsWith("mrmovl"))
            {
                String ra,rb;
                if (curLine.startsWith("rmmovl"))
                {
                    curLine=curLine.substring(6).trim();
                    binaryCode[i]+="40";
                    if (curLine.indexOf(",")<0)
                    {
                        errorMessage="第"+i+"行出现非法指令";
                        return false; 
                    }
                    ra=curLine.substring(0,curLine.indexOf(',')).trim();
                    rb=curLine.substring(curLine.indexOf(',')+1).trim();
                }
                else
                {
                    curLine=curLine.substring(6).trim();
                    binaryCode[i]+="50";
                    if (curLine.indexOf(",")<0)
                    {
                        errorMessage="第"+i+"行出现非法指令";
                        return false; 
                    }
                    rb=curLine.substring(0,curLine.indexOf(',')).trim();
                    ra=curLine.substring(curLine.indexOf(',')+1).trim();
                }
                ra=getRegister(ra);
                if (ra.isEmpty())
                {
                    errorMessage="第"+i+"行出现非法寄存器";
                    return false;
                }
                binaryCode[i]+=ra;
                if (rb.indexOf("(")<0)
                {
                    errorMessage="第"+i+"行出现非法指令";
                    return false;
                }
                String d=rb.substring(0,rb.indexOf("("));
                rb=rb.substring(rb.indexOf("(")+1);
                rb=rb.substring(0,rb.length()-1);
                rb=getRegister(rb);
                if (rb.isEmpty())
                {
                    errorMessage="第"+i+"行出现非法寄存器";
                    return false;
                }
                binaryCode[i]+=rb;
                if (d.isEmpty())
                {
                    binaryCode[i]+="00000000";
                }
                else
                {
                    long tmp=toLong(d);
                    if (error)
                    {
                        errorMessage="第"+i+"行出现非法数值";
                        return false; 
                    }
                    binaryCode[i]+=toLittleEndian(tmp);
                }
                continue;
            }
            if (curLine.startsWith("j")||curLine.startsWith("call"))
            {
                boolean flag=false;
                if (curLine.startsWith("jmp"))
                {
                    flag=true;
                    binaryCode[i]+="70";
                    curLine=curLine.substring(3);
                }
                if (curLine.startsWith("jle"))
                {
                    flag=true;
                    binaryCode[i]+="71";
                    curLine=curLine.substring(3);
                }
                if (curLine.startsWith("jl"))
                {
                    flag=true;
                    binaryCode[i]+="72";
                    curLine=curLine.substring(2);
                }
                if (curLine.startsWith("je"))
                {
                    flag=true;
                    binaryCode[i]+="73";
                    curLine=curLine.substring(2);
                }
                if (curLine.startsWith("jne"))
                {
                    flag=true;
                    binaryCode[i]+="74";
                    curLine=curLine.substring(3);
                }
                if (curLine.startsWith("jge"))
                {
                    flag=true;
                    binaryCode[i]+="75";
                    curLine=curLine.substring(3);
                }
                if (curLine.startsWith("jg"))
                {
                    flag=true;
                    binaryCode[i]+="76";
                    curLine=curLine.substring(2);
                }
                if (curLine.startsWith("call"))
                {
                    flag=true;
                    binaryCode[i]+="80";
                    curLine=curLine.substring(4);
                }
                if (!flag)
                {
                    errorMessage="第"+i+"行出现非法指令";
                    return false;
                }
                labelNum2++;
                labelCall[labelNum2]=curLine.trim();
                labelLine[labelNum2]=i;
                labelOffset[labelNum2]=2;
                binaryCode[i]+="00000000";
                continue;
            }
            if (curLine.startsWith("pushl")||curLine.startsWith("popl"))
            {
                if (curLine.startsWith("pushl"))
                {
                    binaryCode[i]+="a0";
                    curLine=curLine.substring(5).trim();
                }
                else
                {
                    binaryCode[i]+="b0";
                    curLine=curLine.substring(5).trim();
                }
                String ra=getRegister(curLine);
                if (ra.isEmpty())
                {
                    errorMessage="第"+i+"行出现非法寄存器";
                    return false;
                }
                binaryCode[i]+=ra+"f";
                continue;
            }
            errorMessage="第"+i+"行出现非法指令";
            return false;
        }
        for (int i=1;i<=labelNum2;i++)
        {
            boolean flag=false;
            for (int j=1;j<=labelNum;j++)
                if (labelName[j].equals(labelCall[i]))
                {
                    flag=true;
                    binaryCode[labelLine[i]]=binaryCode[labelLine[i]].substring(0,labelOffset[i])+toLittleEndian(labelAddress[j]);
                }
            if (!flag)
            {
                errorMessage="第"+labelLine[i]+"行出现未声明的标记";
                return false;
            }
        }
        for (int i=0;i<=Core.maxMemory;i++)
        {
            Core.memorybak[i]=Core.memory[i]=0;
            Core.modify[i]=false;
        }
        for (int i=0;i<=lineNum;i++)
        {
            if (binaryCode[i].length()==0)  continue;
            if (codeAddress[i]<0||codeAddress[i]+binaryCode[i].length()/2-1>Core.maxMemory)
            {
                errorMessage="第"+i+"行代码地址溢出";
                return false;
            }
            for (int j=0;j<binaryCode[i].length()/2;j++)
            {
                int tmp=(int)codeAddress[i]+j;
                if (Core.modify[tmp])
                {
                    errorMessage="多行代码内存相互覆盖";
                    return false;
                }
                Core.modify[tmp]=true;
                Core.memorybak[tmp]=Core.memory[tmp]=Integer.parseInt(binaryCode[i].substring(j*2,j*2+2),16);
            }
        }
        return true;
    }
    
    static long toLong(String s)
    {
        error=false;
        long res=0;
        try
        {
            if (s.startsWith("0x"))
                res=Long.parseLong(s.substring(2),16);
            else
                res=Long.parseLong(s);
        }
        catch (Exception e)
        {
            error=true;
        }
        return res;
    }
    
    static String toBigEndian(long n)
    {
        String tmp=Long.toHexString(n);
        while (tmp.length()>8)
            tmp=tmp.substring(1);
        while (tmp.length()<8)
            tmp="0"+tmp;
        String res=tmp;
        return res;
    }
    
    static String toLittleEndian(long n)
    {
        String tmp=Long.toHexString(n);
        while (tmp.length()>8)
            tmp=tmp.substring(1);
        while (tmp.length()<8)
            tmp="0"+tmp;
        String res=""+tmp.charAt(6)+tmp.charAt(7)+tmp.charAt(4)+tmp.charAt(5)+tmp.charAt(2)+tmp.charAt(3)+tmp.charAt(0)+tmp.charAt(1);
        return res;
    }
    
    static String getRegister(String s)
    {
        s=s.trim();
        if (s.equals("%eax"))   return "0";
        if (s.equals("%ecx"))   return "1";
        if (s.equals("%edx"))   return "2";
        if (s.equals("%ebx"))   return "3";
        if (s.equals("%esp"))   return "4";
        if (s.equals("%ebp"))   return "5";
        if (s.equals("%esi"))   return "6";
        if (s.equals("%edi"))   return "7";
        return "";
    }
}
