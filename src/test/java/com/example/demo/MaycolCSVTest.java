package com.example.demo;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class MaycolCSVTest {

    @Test
    void converterData(){
        List<Player> list = CsvUtilFile.getPlayers();
        assert list.size() == 18207;
    }


    //FILTRO DE JUGADORES MAYORES DE 34 AÑOS POR STREAM
    @Test
    void stream_playersFilterAgeOver34() {
        List<Player> list = CsvUtilFile.getPlayers();
        Map<String, List<Player>> listFilter = list.parallelStream()
                .filter(player -> player.getAge() >= 34) //mayores o iguales a 34
                .map(player -> {
                    player.name = player.name.toUpperCase(Locale.ROOT);
                    return player;
                })
                .flatMap(playerA -> list.parallelStream() //filtra a los jugadores por club
                        .filter(playerB -> playerA.club.equals(playerB.club))
                )
                .distinct()
                .collect(Collectors.groupingBy(Player::getClub));

        System.out.println(listFilter.size());
//        listFilter.entrySet().stream().forEach(System.out::println);
        assert listFilter.size() == 451;
    }

    @Test
    void reactive_playersFilterAgeOver34() {
        List<Player> list = CsvUtilFile.getPlayers();
        
    }
}
