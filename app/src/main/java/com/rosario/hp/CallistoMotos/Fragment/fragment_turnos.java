package com.rosario.hp.CallistoMotos.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.rosario.hp.CallistoMotos.Adapter.turnoAdapter;
import com.rosario.hp.CallistoMotos.Entidades.turno;
import com.rosario.hp.CallistoMotos.R;
import com.rosario.hp.CallistoMotos.include.Constantes;
import com.rosario.hp.CallistoMotos.include.VolleySingleton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class fragment_turnos extends Fragment {

    private JsonObjectRequest myRequest;
    private static final String TAG = fragment_turnos.class.getSimpleName();
    private RecyclerView lista;
    private TextView texto;
    private ImageView imagen;
    private RecyclerView.LayoutManager lManager;
    private String ls_id_conductor;
    SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<turno> datos;
    private Activity act;
    private turnoAdapter TurnoAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main_basico, container, false);

        datos = new ArrayList<>();

        act = getActivity();

        swipeRefreshLayout = v.findViewById(R.id.swipeRefreshLayout);

        lista =  v.findViewById(R.id.reciclador);
        lista.setHasFixedSize(true);


        texto =  v.findViewById(R.id.TwEmpty);
        imagen = v.findViewById(R.id.ImEmpty);

        imagen.setVisibility(v.INVISIBLE);
        texto.setVisibility(v.INVISIBLE);

        lManager = new LinearLayoutManager(getActivity());
        lista.setLayoutManager(lManager);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        ls_id_conductor     = settings.getString("id","");

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("swipe","swipe");

                swipeRefreshLayout.setRefreshing(true);
                cargarDatos();
            }
        });
        cargarDatos();


        return v;
    }

    public void cargarDatos() {

        // A??adir par??metro a la URL del web service
        String newURL = Constantes.GET_TURNOS + "?conductor=" + ls_id_conductor;
        Log.d(TAG,newURL);

        // Realizar petici??n GET_BY_ID
        VolleySingleton.getInstance(getContext()).addToRequestQueue(
                myRequest = new JsonObjectRequest(
                        Request.Method.POST,
                        newURL,
                        null,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                // Procesar respuesta Json
                                procesarRespuesta(response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "Error Volley viaje: " + error.getMessage());

                            }
                        }
                )
        );
        myRequest.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                5,//DefaultRetryPolicy.DEFAULT_MAX_RETRIES
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

    }

    private void procesarRespuesta(JSONObject response) {
        try {
            // Obtener atributo "estado"
            String estado = response.getString("estado");

            switch (estado) {
                case "1": // EXITO

                    JSONArray mensaje = response.getJSONArray("turno");

                    datos.clear();

                    for(int i = 0; i < mensaje.length(); i++)
                    {JSONObject object = mensaje.getJSONObject(i);
                        turno tur = new turno();

                        String id = object.getString("ID");

                        tur.setId(id);

                        String fecha = object.getString("FECHA");

                        tur.setFecha(fecha);

                        String hora_inicio = object.getString("HORA_INICIO");

                        tur.setHora_inicio(hora_inicio);

                        String hora_fin = object.getString("HORA_FIN");

                        tur.setHora_fin(hora_fin);

                        String recaudacion = object.getString("RECAUDACION");

                        tur.setRecaudacion(recaudacion);

                        String estado_turno = object.getString("ESTADO");

                        tur.setEstado(estado_turno);

                        String nro_turno = object.getString("NRO_TURNO");

                        tur.setNro_turno(nro_turno);

                        datos.add(tur);

                    }

                    TurnoAdapter = new turnoAdapter(getContext(),datos, act);
                    // Setear adaptador a la lista
                    lista.setAdapter(TurnoAdapter);

                    break;
                case "2": // FALLIDO

                    imagen.setVisibility(getView().VISIBLE);
                    texto.setVisibility(getView().VISIBLE);


                    break;
            }
            swipeRefreshLayout.setRefreshing(false);

        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }

    }
}
