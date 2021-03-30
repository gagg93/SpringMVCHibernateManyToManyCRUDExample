package com.websystique.springmvc.dao;

import com.websystique.springmvc.model.Auto;
import com.websystique.springmvc.model.Prenotazione;
import com.websystique.springmvc.model.User;

import java.util.List;


public interface PrenotazioneDao {

	Prenotazione findById(int id);
	
	List<Prenotazione> findByUser(User user);

	List<Prenotazione> findByAuto(Auto auto);
	
	void save(Prenotazione prenotazione);
	
	void deleteById(int id);
	
	List<Prenotazione> findAllPrenotaziones();

}

