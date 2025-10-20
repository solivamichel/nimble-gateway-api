package com.nimble.gateway.util;

public class CpfValidator {

    public static boolean isValid(String cpf) {
        if (cpf == null) return false;
        cpf = cpf.replaceAll("\\D", "");
        if (cpf.length() != 11) return false;
        if (cpf.matches("(\\d)\\1{10}")) return false;
        try {
            int sum=0; for(int i=0;i<9;i++) sum += (cpf.charAt(i)-'0')*(10-i);
            int d1 = 11 - (sum % 11); d1 = (d1>9)?0:d1;
            sum=0; for(int i=0;i<10;i++) sum += (cpf.charAt(i)-'0')*(11-i);
            int d2 = 11 - (sum % 11); d2 = (d2>9)?0:d2;
            return d1==(cpf.charAt(9)-'0') && d2==(cpf.charAt(10)-'0');
        } catch(Exception e){ return false; }
    }
}