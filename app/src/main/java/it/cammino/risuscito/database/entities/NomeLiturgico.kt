package it.cammino.risuscito.database.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
class NomeLiturgico {

    @PrimaryKey
    var idIndice: Int = 0

    var nome: String? = null

    companion object {
        fun defaultData(): ArrayList<NomeLiturgico> {
            val mList = ArrayList<NomeLiturgico>()

            var mDato = NomeLiturgico()
            mDato.idIndice = 1
            mDato.nome = "tempo_avvento"
            mList.add(mDato)

            mDato = NomeLiturgico()
            mDato.idIndice = 2
            mDato.nome = "tempo_natale"
            mList.add(mDato)

            mDato = NomeLiturgico()
            mDato.idIndice = 3
            mDato.nome = "tempo_quaresima"
            mList.add(mDato)

            mDato = NomeLiturgico()
            mDato.idIndice = 4
            mDato.nome = "tempo_pasqua"
            mList.add(mDato)

            mDato = NomeLiturgico()
            mDato.idIndice = 5
            mDato.nome = "canti_pentecoste"
            mList.add(mDato)

            mDato = NomeLiturgico()
            mDato.idIndice = 6
            mDato.nome = "canti_vergine"
            mList.add(mDato)

            mDato = NomeLiturgico()
            mDato.idIndice = 7
            mDato.nome = "lodi_vespri"
            mList.add(mDato)

            mDato = NomeLiturgico()
            mDato.idIndice = 8
            mDato.nome = "canti_ingresso"
            mList.add(mDato)

            mDato = NomeLiturgico()
            mDato.idIndice = 9
            mDato.nome = "canti_pace"
            mList.add(mDato)

            mDato = NomeLiturgico()
            mDato.idIndice = 10
            mDato.nome = "canti_pane"
            mList.add(mDato)

            mDato = NomeLiturgico()
            mDato.idIndice = 11
            mDato.nome = "canti_vino"
            mList.add(mDato)

            mDato = NomeLiturgico()
            mDato.idIndice = 12
            mDato.nome = "canti_fine"
            mList.add(mDato)

            return mList
        }
    }
}
