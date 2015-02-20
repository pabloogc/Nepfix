package com.nepfix.sim;


import com.google.gson.GsonBuilder;
import com.nepfix.sim.nep.Nep;
import com.nepfix.sim.nep.NepBlueprint;
import com.nepfix.sim.nep.NepReader;
import com.nepfix.sim.request.ComputationRequest;

import java.io.InputStreamReader;
import java.util.List;

public class PumpTest {
    private static final int MAX=1000;

    public static void main(String[] args) {
        ComputationRequest request = randomPumpInput();

        NepBlueprint blueprint = NepReader.load(new InputStreamReader(PumpTest.class.getClassLoader().getResourceAsStream("Pump.json")));
        Nep nep = blueprint.create();

        List<String> output = nep.compute(request);

        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(output));
    }

    public static ComputationRequest randomPumpInput() {
        String randomInput = "w";
        int rs,rS,rp,rP,rA,rF;

        rs= (int)(Math.random()*MAX)+1;
        rS= (int)(Math.random()*MAX)+1;
        rp=(int)(Math.random()*MAX)+1;
        rP= (int)(Math.random()*MAX)+1;
        rA=(int)(Math.random()*MAX)+1;
        rF= (int)(Math.random()*MAX)+1;

        StringBuffer winicial3 = new StringBuffer(crearPalabraInicial(rs,rS,rp,rP,rA,rF));

       // System.out.println(">>>>>>>>>>>>>>>>>"+rs+"-"+rS+"-"+rp+"-"+rP+"-"+rA+"-"+rF);

        randomInput=winicial3.toString();
        return new ComputationRequest(randomInput, "", 100);
    }
    private  static String crearPalabraInicial(int a, int b, int c, int d, int e, int f){
        String w="";
        int i=0;
        for( i=0;i<a;i++){
            w+='s';
        }
        for( i=0;i<b;i++){
            w+='S';
        }
        for( i=0;i<c;i++){
            w+='p';
        }
        for( i=0;i<d;i++){
            w+='P';
        }
        for( i=0;i<e;i++){
            w+='A';
        }
        for( i=0;i<f;i++){
            w+='f';
        }
        return w;

    }
}
