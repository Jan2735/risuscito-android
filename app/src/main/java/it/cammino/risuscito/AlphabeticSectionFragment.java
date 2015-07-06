package it.cammino.risuscito;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import it.cammino.risuscito.adapters.CantoAdapter;
import it.cammino.risuscito.objects.CantoRecycled;
import it.cammino.risuscito.ui.RisuscitoSectionTitleIndicator;
import it.cammino.risuscito.utils.ThemeUtils;
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;


public class AlphabeticSectionFragment extends Fragment implements View.OnCreateContextMenuListener  {

    private List<CantoRecycled> titoli;
    private DatabaseCanti listaCanti;
    private String titoloDaAgg;
    private int idDaAgg;
    private int idListaDaAgg;
    private int posizioneDaAgg;
    private ListaPersonalizzata[] listePers;
    private int[] idListe;
    private int idListaClick;
    private int idPosizioneClick;
    private int prevOrientation;

    private final int ID_FITTIZIO = 99999999;
    private final int ID_BASE = 100;

    private LUtils mLUtils;

    private long mLastClickTime = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_alphanum_index, container, false);

        //crea un istanza dell'oggetto DatabaseCanti
        listaCanti = new DatabaseCanti(getActivity());

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        // lancia la ricerca di tutti i titoli presenti in DB e li dispone in ordine alfabetico
        String query = "SELECT _id, titolo, color, pagina, source" +
                "		FROM ELENCO" +
                "		ORDER BY TITOLO ASC";
        Cursor lista = db.rawQuery(query, null);

        //recupera il numero di record trovati
        int total = lista.getCount();

        // crea un array e ci memorizza i titoli estratti
        titoli = new ArrayList<>();
        lista.moveToFirst();
        for (int i = 0; i < total; i++) {
            titoli.add(new CantoRecycled(lista.getString(1)
                    , lista.getInt(3)
                    , lista.getString(2)
                    , lista.getInt(0)
                    , lista.getString(4)));
            lista.moveToNext();
        }

        // chiude il cursore
        lista.close();

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                    return;
                mLastClickTime = SystemClock.elapsedRealtime();
                // recupera il titolo della voce cliccata
                String idCanto = ((TextView) v.findViewById(R.id.text_id_canto))
                        .getText().toString();
                String source = ((TextView) v.findViewById(R.id.text_source_canto))
                        .getText().toString();

                // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare
                Bundle bundle = new Bundle();
                bundle.putString("pagina", source);
                bundle.putInt("idCanto", Integer.parseInt(idCanto));

                // lancia l'activity che visualizza il canto passando il parametro creato
                startSubActivity(bundle, v);
            }
        };

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.cantiList);
        VerticalRecyclerViewFastScroller fastScroller =
                (VerticalRecyclerViewFastScroller) rootView.findViewById(R.id.fast_scroller);

        CantoAdapter adapter = new CantoAdapter(titoli, clickListener, this);
        recyclerView.setAdapter(adapter);

        // Connect the recycler to the scroller (to let the scroller scroll the list)
        fastScroller.setRecyclerView(recyclerView);

        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        recyclerView.addOnScrollListener(fastScroller.getOnScrollListener());

        // Connect the scroller to the adapter to observe data set changes
        adapter.registerAdapterDataObserver(fastScroller.getAdapterDataObserver());

        // Setting the layoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        query = "SELECT _id, lista" +
                "		FROM LISTE_PERS" +
                "		ORDER BY _id ASC";
        lista = db.rawQuery(query, null);

        listePers = new ListaPersonalizzata[lista.getCount()];
        idListe = new int[lista.getCount()];

        lista.moveToFirst();
        for (int i = 0; i < lista.getCount(); i++) {
            idListe[i] = lista.getInt(0);
            listePers[i] = (ListaPersonalizzata) ListaPersonalizzata.
                    deserializeObject(lista.getBlob(1));
            lista.moveToNext();
        }

        lista.close();
        db.close();

        mLUtils = LUtils.getInstance(getActivity());

        if (!mLUtils.hasL()) {
            Drawable myDrawable = getResources().getDrawable(R.drawable.rvfs_fast_scroller_handle_rounded);
            Drawable compatDrawable  = DrawableCompat.wrap(myDrawable);
            DrawableCompat.setTint(compatDrawable, getThemeUtils().accentColor());
            fastScroller.setHandleBackground(compatDrawable);
        }

        RisuscitoSectionTitleIndicator indicator = (RisuscitoSectionTitleIndicator)
                rootView.findViewById(R.id.rvfs_scroll_section_indicator)  ;
        indicator.setIndicatorBackgroundColor(getThemeUtils().accentColor());

        return rootView;
    }

    @Override
    public void onDestroy() {
        if (listaCanti != null)
            listaCanti.close();
        super.onDestroy();
    }

    private void startSubActivity(Bundle bundle, View view) {
        Intent intent = new Intent(getActivity(), PaginaRenderActivity.class);
        intent.putExtras(bundle);
        mLUtils.startActivityWithTransition(intent, view, Utility.TRANS_PAGINA_RENDER);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
//        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        titoloDaAgg = ((TextView) v.findViewById(R.id.text_title)).getText().toString();
        idDaAgg = Integer.valueOf(((TextView) v.findViewById(R.id.text_id_canto)).getText().toString());
        menu.setHeaderTitle("Aggiungi canto a:");

        for (int i = 0; i < idListe.length; i++) {
            SubMenu subMenu = menu.addSubMenu(ID_FITTIZIO, Menu.NONE, 10+i, listePers[i].getName());
            for (int k = 0; k < listePers[i].getNumPosizioni(); k++) {
                subMenu.add(ID_BASE + i, k, k, listePers[i].getNomePosizione(k));
            }
        }

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.add_to, menu);

        SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(getActivity());
        menu.findItem(R.id.add_to_p_pace).setVisible(pref.getBoolean(Utility.SHOW_PACE, false));
        menu.findItem(R.id.add_to_e_seconda).setVisible(pref.getBoolean(Utility.SHOW_SECONDA, false));
        menu.findItem(R.id.add_to_e_santo).setVisible(pref.getBoolean(Utility.SHOW_SANTO, false));

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (getUserVisibleHint()) {
            switch (item.getItemId()) {
                case R.id.add_to_favorites:
//                    addToFavorites(titoloDaAgg);
                    addToFavorites();
                    return true;
                case R.id.add_to_p_iniziale:
//                    addToListaNoDup(1, 1, titoloDaAgg);
                    addToListaNoDup(1, 1);
                    return true;
                case R.id.add_to_p_prima:
//                    addToListaNoDup(1, 2, titoloDaAgg);
                    addToListaNoDup(1, 2);
                    return true;
                case R.id.add_to_p_seconda:
//                    addToListaNoDup(1, 3, titoloDaAgg);
                    addToListaNoDup(1, 3);
                    return true;
                case R.id.add_to_p_terza:
//                    addToListaNoDup(1, 4, titoloDaAgg);
                    addToListaNoDup(1, 4);
                    return true;
                case R.id.add_to_p_pace:
//                    addToListaNoDup(1, 6, titoloDaAgg);
                    addToListaNoDup(1, 6);
                    return true;
                case R.id.add_to_p_fine:
//                    addToListaNoDup(1, 5, titoloDaAgg);
                    addToListaNoDup(1, 5);
                    return true;
                case R.id.add_to_e_iniziale:
//                    addToListaNoDup(2, 1, titoloDaAgg);
                    addToListaNoDup(2, 1);
                    return true;
                case R.id.add_to_e_seconda:
//                    addToListaNoDup(2, 6, titoloDaAgg);
                    addToListaNoDup(2, 6);
                    return true;
                case R.id.add_to_e_pace:
//                    addToListaNoDup(2, 2, titoloDaAgg);
                    addToListaNoDup(2, 2);
                    return true;
                case R.id.add_to_e_santo:
//                    addToListaNoDup(2, 7, titoloDaAgg);
                    addToListaNoDup(2, 7);
                    return true;
                case R.id.add_to_e_pane:
//                    addToListaDup(2, 3, titoloDaAgg);
                    addToListaDup(2, 3);
                    return true;
                case R.id.add_to_e_vino:
//                    addToListaDup(2, 4, titoloDaAgg);
                    addToListaDup(2, 4);
                    return true;
                case R.id.add_to_e_fine:
//                    addToListaNoDup(2, 5, titoloDaAgg);
                    addToListaNoDup(2, 5);
                    return true;
                default:
                    idListaClick = item.getGroupId();
                    idPosizioneClick = item.getItemId();
                    if (idListaClick != ID_FITTIZIO && idListaClick >= 100) {
                        idListaClick -= 100;

                        //recupero ID del canto cliccato
//                        String cantoCliccatoNoApex = Utility.duplicaApostrofi(titoloDaAgg);
//                        SQLiteDatabase db = listaCanti.getReadableDatabase();
//                        String query = "SELECT _id" +
//                                "		FROM ELENCO" +
//                                "		WHERE titolo = '" + cantoCliccatoNoApex + "'";
//                        Cursor cursor = db.rawQuery(query, null);
//                        cursor.moveToFirst();
//                        idDaAgg = cursor.getInt(0);
//                        cursor.close();

                        SQLiteDatabase db = listaCanti.getReadableDatabase();

                        if (listePers[idListaClick]
                                .getCantoPosizione(idPosizioneClick).equals("")) {
                            listePers[idListaClick].addCanto(String.valueOf(idDaAgg), idPosizioneClick);
                            ContentValues  values = new  ContentValues( );
                            values.put("lista" , ListaPersonalizzata.serializeObject(listePers[idListaClick]));
                            db.update("LISTE_PERS", values, "_id = " + idListe[idListaClick], null );

                            Toast.makeText(getActivity()
                                    , getString(R.string.list_added), Toast.LENGTH_SHORT).show();
                        }
                        else {
                            if (listePers[idListaClick].getCantoPosizione(idPosizioneClick).equals(String.valueOf(idDaAgg))) {
                                Toast.makeText(getActivity()
                                        , getString(R.string.present_yet), Toast.LENGTH_SHORT).show();
                            }
                            else {
                                prevOrientation = getActivity().getRequestedOrientation();
                                Utility.blockOrientation(getActivity());
                                //recupero titolo del canto presente
                                String query = "SELECT titolo" +
                                        "		FROM ELENCO" +
                                        "		WHERE _id = "
                                        + listePers[idListaClick].getCantoPosizione(idPosizioneClick);
                                Cursor cursor = db.rawQuery(query, null);
                                cursor.moveToFirst();
                                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                                        .title(R.string.dialog_replace_title)
                                        .content(getString(R.string.dialog_present_yet) + " "
                                                + listePers[idListaClick].getCantoPosizione(idPosizioneClick)
                                                .substring(10)
                                                + cursor.getString(0)
                                                + getString(R.string.dialog_wonna_replace))
                                        .positiveText(R.string.confirm)
                                        .negativeText(R.string.dismiss)
                                        .callback(new MaterialDialog.ButtonCallback() {
                                            @Override
                                            public void onPositive(MaterialDialog dialog) {
                                                SQLiteDatabase db = listaCanti.getReadableDatabase();
                                                listePers[idListaClick].addCanto(String.valueOf(idDaAgg), idPosizioneClick);

                                                ContentValues values = new ContentValues();
                                                values.put("lista", ListaPersonalizzata.serializeObject(listePers[idListaClick]));
                                                db.update("LISTE_PERS", values, "_id = " + idListe[idListaClick], null);
                                                db.close();
                                                getActivity().setRequestedOrientation(prevOrientation);
                                                Toast.makeText(getActivity()
                                                        , getString(R.string.list_added), Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onNegative(MaterialDialog dialog) {
                                                getActivity().setRequestedOrientation(prevOrientation);
                                            }
                                        })
                                        .show();
                                dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                                    @Override
                                    public boolean onKey(DialogInterface arg0, int keyCode,
                                                         KeyEvent event) {
                                        if (keyCode == KeyEvent.KEYCODE_BACK
                                                && event.getAction() == KeyEvent.ACTION_UP) {
                                            arg0.dismiss();
                                            getActivity().setRequestedOrientation(prevOrientation);
                                            return true;
                                        }
                                        return false;
                                    }
                                });
                                dialog.setCancelable(false);
                                cursor.close();
                            }
                        }
                        db.close();
                        return true;
                    }
                    else
                        return super.onContextItemSelected(item);
            }
        }
        else
            return false;
    }

    //aggiunge il canto premuto ai preferiti
//    public void addToFavorites(String titolo) {
    public void addToFavorites() {

        SQLiteDatabase db = listaCanti.getReadableDatabase();

//        String titoloNoApex = Utility.duplicaApostrofi(titolo);

        String sql = "UPDATE ELENCO" +
                "  SET favourite = 1" +
//                "  WHERE titolo =  \'" + titoloNoApex + "\'";
                "  WHERE _id =  " + idDaAgg;
        db.execSQL(sql);
        db.close();

        Toast toast = Toast.makeText(getActivity()
                , getString(R.string.favorite_added), Toast.LENGTH_SHORT);
        toast.show();

    }

    //aggiunge il canto premuto ad una lista e in una posizione che ammetta duplicati
//    public void addToListaDup(int idLista, int listPosition, String titolo) {
    public void addToListaDup(int idLista, int listPosition) {

//        String titoloNoApex = Utility.duplicaApostrofi(titolo);

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        String sql = "INSERT INTO CUST_LISTS ";
        sql+= "VALUES (" + idLista + ", "
                + listPosition + ", "
//                + "(SELECT _id FROM ELENCO"
//                + " WHERE titolo = \'" + titoloNoApex + "\')"
                + idDaAgg
                + ", CURRENT_TIMESTAMP)";

        try {
            db.execSQL(sql);
            Toast.makeText(getActivity()
                    , getString(R.string.list_added), Toast.LENGTH_SHORT).show();
        } catch (SQLException e) {
            Toast toast = Toast.makeText(getActivity()
                    , getString(R.string.present_yet), Toast.LENGTH_SHORT);
            toast.show();
        }

        db.close();
    }

    //aggiunge il canto premuto ad una lista e in una posizione che NON ammetta duplicati
//    public void addToListaNoDup(int idLista, int listPosition, String titolo) {
    public void addToListaNoDup(int idLista, int listPosition) {

//        String titoloNoApex = Utility.duplicaApostrofi(titolo);

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        // cerca se la posizione nella lista � gi� occupata
        String query = "SELECT B.titolo" +
                "		FROM CUST_LISTS A" +
                "		   , ELENCO B" +
                "		WHERE A._id = " + idLista +
                "         AND A.position = " + listPosition +
                "         AND A.id_canto = B._id";
        Cursor lista = db.rawQuery(query, null);

        int total = lista.getCount();

        if (total > 0) {
            lista.moveToFirst();
            String titoloPresente = lista.getString(0);
            lista.close();
            db.close();

            if (titoloDaAgg.equalsIgnoreCase(titoloPresente)) {
                Toast toast = Toast.makeText(getActivity()
                        , getString(R.string.present_yet), Toast.LENGTH_SHORT);
                toast.show();
            }
            else {
                idListaDaAgg = idLista;
                posizioneDaAgg = listPosition;

                prevOrientation = getActivity().getRequestedOrientation();
                Utility.blockOrientation(getActivity());
                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .title(R.string.dialog_replace_title)
                        .content(getString(R.string.dialog_present_yet) + " " + titoloPresente
                                + getString(R.string.dialog_wonna_replace))
                        .positiveText(R.string.confirm)
                        .negativeText(R.string.dismiss)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                SQLiteDatabase db = listaCanti.getReadableDatabase();
//                                String cantoCliccatoNoApex = Utility.duplicaApostrofi(titoloDaAgg);
                                String sql = "UPDATE CUST_LISTS"
//                                        + " SET id_canto = (SELECT _id  FROM ELENCO"
//                                        + " WHERE titolo = \'" + cantoCliccatoNoApex + "\')"
                                        + " SET id_canto = " + idDaAgg
                                        + " WHERE _id = " + idListaDaAgg
                                        + " AND position = " + posizioneDaAgg;
                                db.execSQL(sql);
                                db.close();
                                getActivity().setRequestedOrientation(prevOrientation);
                                Toast.makeText(getActivity()
                                        , getString(R.string.list_added), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                getActivity().setRequestedOrientation(prevOrientation);
                            }
                        })
                        .show();
                dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK
                                && event.getAction() == KeyEvent.ACTION_UP) {
                            arg0.dismiss();
                            getActivity().setRequestedOrientation(prevOrientation);
                            return true;
                        }
                        return false;
                    }
                });
                dialog.setCancelable(false);
            }
            return;
        }

        lista.close();

        String sql = "INSERT INTO CUST_LISTS "
                + "VALUES (" + idLista + ", "
                + listPosition + ", "
//                + "(SELECT _id FROM ELENCO"
//                + " WHERE titolo = \'" + titoloNoApex + "\')"
                + idDaAgg
                + ", CURRENT_TIMESTAMP)";
        db.execSQL(sql);
        db.close();

        Toast.makeText(getActivity()
                , getString(R.string.list_added), Toast.LENGTH_SHORT).show();
    }

    private ThemeUtils getThemeUtils() {
        return ((MainActivity)getActivity()).getThemeUtils();
    }

}