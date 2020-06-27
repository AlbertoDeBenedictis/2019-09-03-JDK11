package it.polito.tdp.food.model;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.food.db.FoodDao;

public class Model {

	FoodDao dao;
	Graph<String, DefaultWeightedEdge> grafo;
	List<String> listaPorzioni;
	List<String> camminoPesoMassimo;
	Double pesoMax;

	public Model() {
		dao = new FoodDao();
	}

	public List<String> getVertici() {
		return this.listaPorzioni;
	}

	public Integer getArchi() {
		return this.grafo.edgeSet().size();
	}

	public List<PorzioneAdiacente> getAdiacenti(String porzione) {
		List<String> vicini = Graphs.neighborListOf(this.grafo, porzione);

		List<PorzioneAdiacente> listaAdiacenti = new ArrayList<>();

		for (String st : vicini) {

			Double peso = (Double) this.grafo.getEdgeWeight(this.grafo.getEdge(porzione, st));

			listaAdiacenti.add(new PorzioneAdiacente(st, peso));

		}

		return listaAdiacenti;

	}

	public List<String> getCammino(String porzione, int N) {

		pesoMax = 0.0;

		List<String> parziale = new ArrayList<>();
		parziale.add(porzione);

		camminoPesoMassimo = new ArrayList<>();

		// inizio la ricorsione
		ricorsione(parziale, N);
		
		return this.camminoPesoMassimo;

	}

	public Double getPeso(List<String> cammino) {

		Double peso = 0.0;

		for (int i = 0; i < cammino.size() - 1; i++) {

			peso += this.grafo.getEdgeWeight(this.grafo.getEdge(cammino.get(i), cammino.get(i + 1)));
		}

		return peso;

	}

	private void ricorsione(List<String> parziale, int N) {

		// CASO TERMINALE
		if (parziale.size() == N) {

			Double pesoCorrente = getPeso(parziale);

			// se il peso è maggiore, aggiorno la soluzione
			if (pesoCorrente > pesoMax) {
				this.camminoPesoMassimo = new ArrayList<>(parziale);
				pesoMax = pesoCorrente;
			}
			return;

		}

		// CREO NUOVE SOLUZIONI
		// scorro tra i vicini dell'ultimo
		for (String porzione : Graphs.neighborListOf(this.grafo, parziale.get(parziale.size() - 1))) {

			// se la porzione non l'ho ancora messa
			if (!parziale.contains(porzione)) {

				parziale.add(porzione);
				ricorsione(parziale, N);
				parziale.remove(porzione);
			}

		}

	}

	public void creaGrafo(Integer calorie) {

		listaPorzioni = new ArrayList<>();

		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

		// Importiamo i vertici
		listaPorzioni = this.dao.getPorzioni(calorie);
		Graphs.addAllVertices(this.grafo, listaPorzioni);

		// Ora ci occupiamo degli archi

		// prendo le adiacenze
		List<Adiacenza> adiacenze = this.dao.getAdiacenze();

		for (Adiacenza a : adiacenze) {

			// se entrambi i vertici fanno parte del grafo
			if (this.grafo.containsVertex(a.getPorzione1()) && this.grafo.containsVertex(a.getPorzione2())) {

				// se non esiste ancora un arco, lo creo
				if (this.grafo.getEdge(a.getPorzione1(), a.getPorzione2()) == null) {
					Graphs.addEdge(this.grafo, a.getPorzione1(), a.getPorzione2(), a.getPeso());
				}
			}
		}

		System.out.println(
				"Grafo Creato!\n#Vertici " + this.grafo.vertexSet().size() + "\n#Archi " + this.grafo.edgeSet().size());

	}

}
