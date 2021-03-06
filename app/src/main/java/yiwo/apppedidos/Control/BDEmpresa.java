package yiwo.apppedidos.Control;

import android.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import yiwo.apppedidos.AspectosGenerales.CodigosGenerales;
import yiwo.apppedidos.AspectosGenerales.ConfiguracionEmpresa;
import yiwo.apppedidos.ConexionBD.BDConexionSQL;

public class BDEmpresa {

    BDConexionSQL bdata= new BDConexionSQL();
    String TAG="BDEmpresa";
    public ArrayList<List<String>> getList(Connection connection, String nombre) {

        ArrayList<List<String>> arrayList = new ArrayList<>();

        try {
            if(CodigosGenerales.listEmpresas.size()>0 && nombre.equals("")){
                return  CodigosGenerales.listEmpresas;
            }else {

                String stsql = "select ccod_empresa,crazonsocial,cnum_ruc from Hempresa where (crazonsocial like ?  or ccod_empresa like ?) order by ccod_empresa";
                PreparedStatement query = connection.prepareStatement(stsql);
                query.setString(1, nombre + "%"); //Razón Social de la empresa
                query.setString(2, nombre + "%"); //Código de la empresa
                ResultSet rs = query.executeQuery();
                while (rs.next()) {
                    arrayList.add(Arrays.asList(
                            rs.getString("ccod_empresa"),       //Código de la empresa
                            rs.getString("crazonsocial"),       //Razón Social de la empresa
                            rs.getString("cnum_ruc")            //Número de Ruc de la empresa
                    ));
                }
                if (nombre.equals(""))
                    CodigosGenerales.listEmpresas=arrayList;
            }
        } catch (Exception e) {
            Log.d(TAG, "- getList: "+e.getMessage());
        }
        return arrayList;
    }


    public ArrayList<List<String>> getList( String nombre) {

        ArrayList<List<String>> arrayList = new ArrayList<>();

        try {
            if(CodigosGenerales.listEmpresas.size()>0 && nombre.equals("")){
                return  CodigosGenerales.listEmpresas;
            }else {
                Connection connection= bdata.getConnection();
                String stsql = "select ccod_empresa,crazonsocial,cnum_ruc from Hempresa where (crazonsocial like ?  or ccod_empresa like ?) order by ccod_empresa";
                PreparedStatement query = connection.prepareStatement(stsql);
                query.setString(1, nombre + "%"); //Razón Social de la empresa
                query.setString(2, nombre + "%"); //Código de la empresa
                ResultSet rs = query.executeQuery();
                while (rs.next()) {
                    arrayList.add(Arrays.asList(
                            rs.getString("ccod_empresa"),       //Código de la empresa
                            rs.getString("crazonsocial"),       //Razón Social de la empresa
                            rs.getString("cnum_ruc")            //Número de Ruc de la empresa
                    ));
                }
                if (nombre.equals(""))
                    CodigosGenerales.listEmpresas=arrayList;
                connection.close();
            }
        } catch (Exception e) {
            Log.d(TAG, "- getList: "+e.getMessage());
        }
        return arrayList;
    }


    public  ArrayList<List<String>>  getMonedas(){
        ArrayList<List<String>> arrayList=new ArrayList<>();

        try {

            Connection connection= bdata.getConnection();
            String stsql = "select * from Htipmon where ccod_empresa = ?";

            PreparedStatement query = connection.prepareStatement(stsql);
            query.setString(1, ConfiguracionEmpresa.Codigo_Empresa); //Código de la empresa
            ResultSet rs = query.executeQuery();
            while (rs.next()) {
                arrayList.add(Arrays.asList(
                        rs.getString("cmoneda"),
                        rs.getString("cdes_tipmon"),
                        rs.getString("ccod_tipmon")
                ));

            }
            connection.close();

        } catch (Exception e) {
            Log.d(TAG, "- getMonedas: "+ConfiguracionEmpresa.Codigo_Empresa+" - "+e.getMessage());
        }
        return arrayList;
    }
    public  ArrayList<List<String>>  getMonedas(Connection connection){
        ArrayList<List<String>> arrayList=new ArrayList<>();

        try {

            String stsql = "select * from Htipmon where ccod_empresa = ?";

            PreparedStatement query = connection.prepareStatement(stsql);
            query.setString(1, ConfiguracionEmpresa.Codigo_Empresa); //Código de la empresa
            ResultSet rs = query.executeQuery();
            while (rs.next()) {
                arrayList.add(Arrays.asList(
                        rs.getString("cmoneda"),
                        rs.getString("cdes_tipmon"),
                        rs.getString("ccod_tipmon")
                ));

            }

        } catch (Exception e) {
            Log.d(TAG, "- getMonedas: "+ConfiguracionEmpresa.Codigo_Empresa+" - "+e.getMessage());
        }
        return arrayList;
    }
    public  String isIncluidoIGV(Connection connection){
        try {
            String stsql = "select mas_igv from  Hconfiguraciones_2 where idempresa = ?";
            PreparedStatement query = connection.prepareStatement(stsql);
            query.setString(1, ConfiguracionEmpresa.Codigo_Empresa); //Código de la empresa
            ResultSet rs = query.executeQuery();
            String mas_igv="";
            while (rs.next()) {
                mas_igv=rs.getString("mas_igv");
            }
            return mas_igv;
        } catch (Exception e) {
            Log.d(TAG, "- isIncluidoIGV: "+e.getMessage());
        }
        return "S";
    }
    public String getMonedaTrabajo(Connection connection){
        String  monedaTrabajo = "S/";

        try {
            String stsql = "select moneda_trabajo from Hconfiguraciones_2 where idempresa = ?";
            PreparedStatement query = connection.prepareStatement(stsql);
            query.setString(1, ConfiguracionEmpresa.Codigo_Empresa); //Código de la empresa
            ResultSet rs = query.executeQuery();
            while (rs.next()) {
                monedaTrabajo= rs.getString("moneda_trabajo"); //Tipo de moneda de la empresa
            }
        } catch (Exception e) {
            Log.d(TAG, "- getMonedaTrabajo: "+e.getMessage());
        }
        return monedaTrabajo;
    }
    /*
    ctip	tc
    VTA	3.272
     */
    public  List<String> getDatosTipoCambio(Connection connection){
        //select ntc_venta, ntc_compra,ntc_especial from hcalenda where  CONVERT(date, dfecha) =CONVERT(date, getdate())
        List<String> list=new ArrayList<>();

        try {

            String stsql =
                    "select\n" +
                            "(Select ctipo_cambio from hconfiguraciones_2 where idempresa = ?) Tipo,\n" +
                            "(Select (CASE ctipo_cambio when 'VTA' then ntc_venta When 'COM' then ntc_compra else ntc_especial END) from hconfiguraciones_2 where idempresa = ?) Valor\n" +
                            "from hcalenda where Convert(varchar, dfecha,111) = convert(varchar,getdate(),111)";

            PreparedStatement query = connection.prepareStatement(stsql);
            query.setString(1, ConfiguracionEmpresa.Codigo_Empresa); //Código de la empresa
            query.setString(2, ConfiguracionEmpresa.Codigo_Empresa); //Código de la empresa
            ResultSet rs = query.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("Tipo")); //Valor para el tipo de cambio
                list.add(rs.getString("Valor")); //Tipo de cambio (especial, venta o compra)
            }

        } catch (Exception e) {
            Log.d(TAG, "- getTipoCambio: "+e.getMessage());
        }
        return list;
    }
}
