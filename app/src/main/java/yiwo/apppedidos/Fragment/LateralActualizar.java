package yiwo.apppedidos.Fragment;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import yiwo.apppedidos.AspectosGenerales.CodigosGenerales;
import yiwo.apppedidos.AspectosGenerales.CodigosUtiles;
import yiwo.apppedidos.AspectosGenerales.ConfiguracionEmpresa;
import yiwo.apppedidos.AspectosGenerales.DatosConexiones;
import yiwo.apppedidos.Control.BDArticulos;
import yiwo.apppedidos.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LateralActualizar extends Fragment {
    Button b_actualizar, b_cancelar;
    ProgressBar pb_loading;
    TextView tv_mensaje;
    String TAG = "LateralActualizar";
    BackGroundTask task;
    Integer articulos_descargados = 0, articulos_no_descargados = 0, articulos_totales = 0;

    private DatosConexiones datosConexiones = new DatosConexiones();

    public LateralActualizar() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag_lateral_actualizar, container, false);
        tv_mensaje = view.findViewById(R.id.tv_mensaje);
        b_actualizar = view.findViewById(R.id.b_actualizar);
        b_cancelar = view.findViewById(R.id.b_cancelar);
        pb_loading = view.findViewById(R.id.pb_loading);

        b_actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (CodigosUtiles.isExternalStorageWritable()) {
                        CodigosUtiles.crearCarpetaAlmInterno();
//                        try {
//                            CodigosUtiles.crearDirectorioPrivado(getContext(), "pedidos");
//                        } catch (Exception ex) {
//                            Log.d(TAG, ex.getMessage());
//                        }
                        task = new BackGroundTask();
                        task.execute("");
                    } else {
                        Toast.makeText(getActivity(), "No se puede crear la carpeta en la memoria externa", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.d("CreateView", "Task " + e.getMessage());
                }
            }
        });


        b_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    task.cancel(true);
                    task.onPostExecute("");
                } catch (Exception e) {
                    Log.d("CreateView", "Task " + e.getMessage());
                }
            }
        });


        return view;
    }

    @Override
    public void onPause() {
        if (task != null)
            task.cancel(true);
        super.onPause();
    }

    public class BackGroundTask extends AsyncTask<String, String, String> {
        int level = 0;
        Boolean exito = false;

        @Override
        protected void onPreExecute() {
            Log.d(TAG,"Comenzando task");
            pb_loading.setVisibility(View.VISIBLE);

            CodigosGenerales.CancelarTask = false;
            pb_loading.setProgress(0);
            b_actualizar.setEnabled(false);
            pb_loading.setVisibility(View.VISIBLE);
            b_cancelar.setVisibility(View.VISIBLE);
            tv_mensaje.setVisibility(View.VISIBLE);
            tv_mensaje.setText("Descargando...");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            if (isCancelled()){
                Log.d(TAG,"Cancelando task");
                return null;}
            else {
                Log.d(TAG,"progress task");
                exito = GuardarMenu();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (exito) {
                Log.d(TAG,"Finalizando con exito task");
                if (articulos_descargados == articulos_totales) {

                    Toast.makeText(getContext(), "Actualización finalizada ", Toast.LENGTH_SHORT).show();
                    tv_mensaje.setText("La actualización se ha llevado a cabo con éxito \n Imagenes descargadas: " + articulos_descargados);
                    pb_loading.setProgress(100);
                } else {
                    Toast.makeText(getContext(), "Actualización finalizada ", Toast.LENGTH_SHORT).show();
                    tv_mensaje.setText("La actualización se ha llevado a cabo con algunos problemas....\n Se han podido descargar " + articulos_descargados + " imagenes.");
                    pb_loading.setProgress(100);
                }
            } else {
                Log.d(TAG,"Finalizando task");
                Toast.makeText(getContext(), "Actualización fallida", Toast.LENGTH_SHORT).show();
                tv_mensaje.setText("Hubo un problema en la actualización.");
                pb_loading.setProgress(0);
            }
            b_cancelar.setVisibility(View.GONE);
            b_actualizar.setEnabled(true);
            super.onPostExecute(s);
        }
    }


    public Boolean GuardarMenu() {

//        String ip;

//        if (ConfiguracionEmpresa.isLAN && ConfiguracionEmpresa.isLANAviable)
//            ip = datosConexiones.getIP_LAN();
//        else if (!ConfiguracionEmpresa.isLAN && ConfiguracionEmpresa.isPublicaAviable)
//            ip = datosConexiones.getIP_Publica();
//        else
//            return false;

        String ip=ConfiguracionEmpresa.IP;
        Log.d(TAG,"ip task: "+ip);

        String DOWNLOAD_URL
                = "http://" + ip + ":" + datosConexiones.getPuertoImagenes() + "/" +
                datosConexiones.getIP_LAN() + "/" + datosConexiones.getCarpetaImagenes() + "/";
        Log.d(TAG, "Conectandose a :" + DOWNLOAD_URL);

        GuardarMenu_Articulos(DOWNLOAD_URL);
        GuardarMenu_Familia(DOWNLOAD_URL);
        GuardarMenu_SubFamilia(DOWNLOAD_URL);
        GuardarMenu_Conceptos(DOWNLOAD_URL);

        BDArticulos bdArticulos = new BDArticulos();
        ArrayList<List<String>> arrayList = bdArticulos.getListFull();
        articulos_totales = arrayList.size();
        for (int i = 0; i < arrayList.size(); i++) {
            if (task.isCancelled())
                break;
            if (GuardarArticulos(DOWNLOAD_URL, arrayList.get(i).get(0))) {
                Integer Porcentaje = (100 * i) / articulos_totales;
                pb_loading.setProgress(Porcentaje);
            }

        }
        return true;
    }

    private void GuardarMenu_Articulos(String DOWNLOAD_URL) {
        try {
            String Nombre_Imagen = ConfiguracionEmpresa.Codigo_Empresa + "_articulos.jpg";

            URL url = new URL(DOWNLOAD_URL + Nombre_Imagen);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            InputStream input = connection.getInputStream();

            Bitmap bitmap = BitmapFactory.decodeStream(input);

            //guardar el archivo con el nombre de la imagen en el directorio "myDirectorio"
            File imgFile = new File(DatosConexiones.myDirectorio, Nombre_Imagen);

            FileOutputStream out = new FileOutputStream(imgFile);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

            out.flush();

            out.close();
            Log.d(TAG, "Descargando desde: " + url);
        } catch (Exception e) {
            Log.d(TAG, "GuardarMenu_Articulos Error- " + e.getMessage());
        }
    }

    private void GuardarMenu_Familia(String DOWNLOAD_URL) {
        try {
            String Nombre_Imagen = ConfiguracionEmpresa.Codigo_Empresa + "_familia.jpg";

            URL url = new URL(DOWNLOAD_URL + Nombre_Imagen);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            InputStream input = connection.getInputStream();

            Bitmap bitmap = BitmapFactory.decodeStream(input);

            //guardar el archivo con el nombre de la imagen en el directorio "myDirectorio"
            File imgFile = new File(DatosConexiones.myDirectorio, Nombre_Imagen);

            FileOutputStream out = new FileOutputStream(imgFile);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

            out.flush();

            out.close();
            Log.d(TAG, "Descargando desde: " + url);

        } catch (Exception e) {
            Log.d(TAG, "GuardarMenu_Familia - " + e.getMessage());
        }
    }

    private void GuardarMenu_SubFamilia(String DOWNLOAD_URL) {
        try {
            String Nombre_Imagen = ConfiguracionEmpresa.Codigo_Empresa + "_subfamilia.jpg";

            URL url = new URL(DOWNLOAD_URL + Nombre_Imagen);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            InputStream input = connection.getInputStream();

            Bitmap bitmap = BitmapFactory.decodeStream(input);

            //guardar el archivo con el nombre de la imagen en el directorio "myDirectorio"
            File imgFile = new File(DatosConexiones.myDirectorio, Nombre_Imagen);

            FileOutputStream out = new FileOutputStream(imgFile);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

            out.flush();

            out.close();

            Log.d(TAG, "Descargando desde: " + url);
        } catch (Exception e) {
            Log.d(TAG, "GuardarMenu_SubFamilia - " + e.getMessage());
        }
    }

    private void GuardarMenu_Conceptos(String DOWNLOAD_URL) {
        try {
            for (int i = 1; i <= 7; i++) {
                String Nombre_Imagen = ConfiguracionEmpresa.Codigo_Empresa + "_concepto" + i + ".jpg";

                URL url = new URL(DOWNLOAD_URL + Nombre_Imagen);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream input = connection.getInputStream();

                Bitmap bitmap = BitmapFactory.decodeStream(input);

                //guardar el archivo con el nombre de la imagen en el directorio "myDirectorio"
                File imgFile = new File(DatosConexiones.myDirectorio, Nombre_Imagen);

                FileOutputStream out = new FileOutputStream(imgFile);

                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

                out.flush();

                out.close();
                Log.d(TAG, "Descargando desde: " + url);
            }
        } catch (Exception e) {
            Log.d(TAG, "GuardarMenu_Conceptos - " + e.getMessage());
        }
    }

    private Boolean GuardarArticulos(String DOWNLOAD_URL, String Codigo_Articulo) {
        try {
            for (int i = 1; i <= 4; i++) {
                String Nombre_Imagen = ConfiguracionEmpresa.Codigo_Empresa + "_" + Codigo_Articulo + "_" + i + ".jpg";
                URL url = new URL(DOWNLOAD_URL + Nombre_Imagen);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream input = connection.getInputStream();

                Bitmap bitmap = BitmapFactory.decodeStream(input);

                //guardar el archivo con el nombre de la imagen en el directorio "myDirectorio"
                File imgFile = new File(DatosConexiones.myDirectorio, Nombre_Imagen);

                FileOutputStream out = new FileOutputStream(imgFile);

                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

                out.flush();

                out.close();
                articulos_descargados++;
                Log.d(TAG, "Descargando desde: " + url);
            }
            return true;

        } catch (Exception e) {
            articulos_no_descargados++;
            Log.d(TAG, "GuardarArticulos -" + e.getMessage());
        }
        return false;
    }
}