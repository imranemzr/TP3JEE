package com.example;

import com.example.model.*;
import javax.persistence.*;
import java.time.LocalDate;

public class App {

    public static void main(String[] args) {

        EntityManagerFactory emf = null;
        EntityManager em = null;

        try {
            emf = Persistence.createEntityManagerFactory("gestion-reservations-pu");
            em = emf.createEntityManager();

            Utilisateur utilisateur = new Utilisateur(
                    "Yassine El Amrani",
                    "yassine.tp3@example.com"
            );

            Salle salle = new Salle("Salle Atlas", 35);

            Equipement projecteur = new Equipement("Vidéoprojecteur");
            Equipement tableau = new Equipement("Tableau interactif");

            salle.ajouterEquipement(projecteur);
            salle.ajouterEquipement(tableau);

            Reservation reservation = new Reservation(
                    LocalDate.of(2026, 10, 15),
                    salle
            );

            utilisateur.ajouterReservation(reservation);

            em.getTransaction().begin();

            em.persist(salle);

            em.persist(utilisateur);

            em.getTransaction().commit();

            Long salleId = salle.getId();
            Long utilisateurId = utilisateur.getId();
            Long reservationId = reservation.getId();
            Long projecteurId = projecteur.getId();

            System.out.println("\n=== TEST 1 : ENREGISTREMENT ===");
            System.out.println("Utilisateur : " + utilisateur.getNom());
            System.out.println("Salle : " + salle.getNom());
            System.out.println("Nombre d'équipements : "
                    + salle.getEquipements().size());
            System.out.println("Date de réservation : "
                    + reservation.getDateReservation());

            em.clear();

            Reservation reservationEnregistree =
                    em.find(Reservation.class, reservationId);

            System.out.println("Réservation retrouvée : "
                    + reservationEnregistree);


            System.out.println("\n=== TEST 2 : ORPHAN REMOVAL ===");

            em.getTransaction().begin();

            Utilisateur utilisateurGere =
                    em.find(Utilisateur.class, utilisateurId);

            Reservation reservationASupprimer =
                    utilisateurGere.getReservations().iterator().next();

            utilisateurGere.retirerReservation(reservationASupprimer);

            em.getTransaction().commit();

            em.clear();

            Reservation verificationOrphelin =
                    em.find(Reservation.class, reservationId);

            System.out.println("Réservation après retrait : "
                    + verificationOrphelin);

            System.out.println("Suppression orpheline réussie : "
                    + (verificationOrphelin == null));


            System.out.println("\n=== TEST 3 : CASCADE REMOVE ===");

            Utilisateur deuxiemeUtilisateur = new Utilisateur(
                    "Salma Bennani",
                    "salma.tp3@example.com"
            );

            Reservation deuxiemeReservation = new Reservation(
                    LocalDate.of(2026, 10, 20),
                    em.find(Salle.class, salleId)
            );

            deuxiemeUtilisateur.ajouterReservation(deuxiemeReservation);

            em.getTransaction().begin();

            em.persist(deuxiemeUtilisateur);

            em.getTransaction().commit();

            Long deuxiemeUtilisateurId = deuxiemeUtilisateur.getId();
            Long deuxiemeReservationId = deuxiemeReservation.getId();

            em.clear();

            em.getTransaction().begin();

            Utilisateur utilisateurASupprimer =
                    em.find(Utilisateur.class, deuxiemeUtilisateurId);

            em.remove(utilisateurASupprimer);

            em.getTransaction().commit();

            em.clear();

            Utilisateur utilisateurRestant =
                    em.find(Utilisateur.class, deuxiemeUtilisateurId);

            Reservation reservationRestante =
                    em.find(Reservation.class, deuxiemeReservationId);

            Salle salleRestante =
                    em.find(Salle.class, salleId);

            Equipement equipementRestant =
                    em.find(Equipement.class, projecteurId);

            System.out.println("Utilisateur supprimé : "
                    + (utilisateurRestant == null));

            System.out.println("Réservation supprimée : "
                    + (reservationRestante == null));

            System.out.println("Salle conservée : "
                    + (salleRestante != null));

            System.out.println("Équipement conservé : "
                    + (equipementRestant != null));

            boolean testPersistance = reservationEnregistree != null;
            boolean testOrphelin = verificationOrphelin == null;
            boolean testCascade = utilisateurRestant == null
                    && reservationRestante == null;
            boolean testConservation = salleRestante != null
                    && equipementRestant != null;

            System.out.println("\n========== BILAN TP3 ==========");
            System.out.println("Persistance : " + testPersistance);
            System.out.println("Orphan removal : " + testOrphelin);
            System.out.println("Cascade REMOVE : " + testCascade);
            System.out.println("Entités partagées conservées : "
                    + testConservation);

        } catch (Exception e) {

            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            System.err.println("Erreur pendant le test JPA : "
                    + e.getMessage());

            e.printStackTrace();

        } finally {

            if (em != null && em.isOpen()) {
                em.close();
            }

            if (emf != null && emf.isOpen()) {
                emf.close();
            }
        }
    }
}

