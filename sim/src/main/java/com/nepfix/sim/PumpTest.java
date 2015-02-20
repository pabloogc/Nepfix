package com.nepfix.sim;


import com.google.gson.GsonBuilder;
import com.nepfix.sim.nep.Nep;
import com.nepfix.sim.nep.NepBlueprint;
import com.nepfix.sim.nep.NepReader;
import com.nepfix.sim.request.ComputationRequest;

import java.io.InputStreamReader;
import java.util.List;

public class PumpTest {
    public static void main(String[] args) {
        ComputationRequest request = randomPumpInput();

        NepBlueprint blueprint = NepReader.load(new InputStreamReader(PumpTest.class.getClassLoader().getResourceAsStream("Pump.json")));
        Nep nep = blueprint.create();

        List<String> output = nep.compute(request);

        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(output));
    }

    public static ComputationRequest randomPumpInput() {
        String randomInput = "w";

        /*

                rs= ThreadLocalRandom.current().nextInt(MAX);
                rS= ThreadLocalRandom.current().nextInt(MAX);
                rp= ThreadLocalRandom.current().nextInt(MAX);
                rP= ThreadLocalRandom.current().nextInt(MAX);
                rA=ThreadLocalRandom.current().nextInt(MAX);
                rF= ThreadLocalRandom.current().nextInt(MAX);

                StringBuffer w = new StringBuffer(crearPalabraInicial(rs,rS,rp,rP,rA,rF));
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
                System.out.println(">>>>>>>>>>>>>>>>>"+rs+"-"+rS+"-"+rp+"-"+rP+"-"+rA+"-"+rF);
                return winicial3; //este es un supuesto estado del sistema /célula
            }

            public String getNombre(){
                return this.texto;
            }
            private  String crearPalabraInicial(int a, int b, int c, int d, int e, int f){
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

    */
        return new ComputationRequest(randomInput, "", 100);
    }

}
