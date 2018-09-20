package yiwo.apppedidos.Fragment;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import yiwo.apppedidos.AspectosGenerales.CodigosGenerales;
import yiwo.apppedidos.Data.BDActivarFunciones;
import yiwo.apppedidos.R;
import yiwo.apppedidos.ConexionBD.BDConexionSQLite;


public class FragSplashScreen extends Fragment {

    BDConexionSQLite myDb;
    TextView tv_link;
    String TAG = "FragSplashScreen";
    BDActivarFunciones bdActivarFunciones = new BDActivarFunciones();
    Boolean isConfigurate, isLogin;
    Fragment fragment;
    public FragSplashScreen() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag_splash_screen, container, false);
        tv_link = view.findViewById(R.id.tv_link);
        myDb = new BDConexionSQLite(getContext());
        fragment= new FragLogin();
        try {

            BackGroundTask task = new BackGroundTask();
            task.execute("");

        } catch (Exception e) {
            Log.d(TAG, "onCreateView "+e.getMessage());
        }
        return view;
    }

    //region Tarea en Segundo Plano con ListView
    public class BackGroundTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            ComprobarConfiguraciones();
            if (isConfigurate)
                ComprobarLogin();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            CambiarFragment(fragment);
            super.onPostExecute(s);
        }
    }


    public void CambiarFragment(Fragment fragment) {
        assert getFragmentManager() != null;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        transaction.replace(R.id.frag_contenedor, fragment);
        transaction.commit();
    }


    public void ComprobarConfiguraciones(){
        Cursor datos_configuracion = myDb.getDataConfiguracion();

        List<String> datos_empresa = new ArrayList();

        while (datos_configuracion.moveToNext()) {
            datos_empresa.add(datos_configuracion.getString(1));//Obtener el Codigo_Empresa
            datos_empresa.add(datos_configuracion.getString(2));//Obtener el Codigo_PuntoVenta
            datos_empresa.add(datos_configuracion.getString(3));//Obtener el Codigo_Almacen
            datos_empresa.add(datos_configuracion.getString(4));//Obtener el Codigo_Usuario
        }
        if(datos_empresa.isEmpty()) {
            isConfigurate=false;
            fragment = new FragConfiguracion();
            Log.d(TAG, "Vacío: " + datos_empresa);
        }else{
            Log.d(TAG, "Con Datos: " + datos_empresa);
            isConfigurate=true;
        }
    }

    public void ComprobarLogin(){

        bdActivarFunciones.ActualizarFunciones();
        Cursor datos_login = myDb.getDataLogin();
        isLogin = datos_login.getCount() != 0;
        //endregion

        //region Verificar si el usuario ha ingresado previamente obteniendo los datos del SQLITE
        if (isLogin) {
            //region Configuración para cuando ya se ha iniciado sesión previamente
            List<String> datos_usuario = new ArrayList();
            while (datos_login.moveToNext()) {
                datos_usuario.add(datos_login.getString(1));//Obtener el Codigo_Empresa
                datos_usuario.add(datos_login.getString(2));//Obtener el Codigo_PuntoVenta
                datos_usuario.add(datos_login.getString(3));//Obtener el Codigo_Almacen
                datos_usuario.add(datos_login.getString(4));//Obtener el Codigo_Usuario
                datos_usuario.add(datos_login.getString(5));//Obtener el Codigo_CentroCostos
                datos_usuario.add(datos_login.getString(6));//Obtener el Codigo_UnidadNegocio
                datos_usuario.add(datos_login.getString(7));//Obtener el Tipo_Moneda
                datos_usuario.add(datos_login.getString(8));//Obtener la direccion del almacen
                datos_usuario.add(datos_login.getString(9));//Obtener el Nombre del Vendedor
                datos_usuario.add(datos_login.getString(10));//Obtener el Celular del Vendedor
                datos_usuario.add(datos_login.getString(11));//Obtener el email del Vendedor
            }
            CodigosGenerales.Codigo_Empresa = datos_usuario.get(0);   //Guardar el Codigo_Empresa en CodigosGenerales
            CodigosGenerales.Codigo_PuntoVenta = datos_usuario.get(1);   //Guardar el Codigo_PuntoVenta en CodigosGenerales
            CodigosGenerales.Codigo_Almacen = datos_usuario.get(2);   //Guardar el Codigo_Almacen en CodigosGenerales
            CodigosGenerales.Codigo_Usuario = datos_usuario.get(3);   //Guardar el Codigo_Usuario en CodigosGenerales
            CodigosGenerales.Codigo_CentroCostos = datos_usuario.get(4);   //Guardar el Codigo_CentroCostos en CodigosGenerales
            CodigosGenerales.Codigo_UnidadNegocio = datos_usuario.get(5);   //Guardar el Codigo_UnidadNegocio en CodigosGenerales
            CodigosGenerales.Moneda_Empresa = datos_usuario.get(6);   //Guardar el Codigo_UnidadNegocio en CodigosGenerales
            CodigosGenerales.Direccion_Almacen = datos_usuario.get(7);   //Guardar la direccion del almacen en CodigosGenerales
            CodigosGenerales.Nombre_Vendedor = datos_usuario.get(8);   //Guardar el Nombre del Vendedor
            CodigosGenerales.Celular_Vendedor = datos_usuario.get(9);   //Guardar el Celular del Vendedor
            CodigosGenerales.email_Vendedor = datos_usuario.get(10);   //Guardar el email del Vendedor
//                    CodigosGenerales.TipoArray = "Articulos";
//                    CodigosGenerales.getArrayListArticulos("");
            //endregion

            Log.d(TAG, "datos_usuario - " + CodigosGenerales.Codigo_Empresa);
            Log.d(TAG, "datos_usuario - " + datos_usuario.get(0));

            fragment = new FragMenuPrincipal();
        } else {
            CodigosGenerales.TipoArray = "Empresa";
            CodigosGenerales.getArrayList("");
        }
        datos_login.close();        //Cerrar el cursor para liberar el sqlite
    }

}