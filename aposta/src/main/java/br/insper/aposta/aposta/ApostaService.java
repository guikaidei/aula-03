package br.insper.aposta.aposta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApostaService {

    @Autowired
    private ApostaRepository apostaRepository;

    public void salvar(Aposta aposta) {
        aposta.setId(UUID.randomUUID().toString());

        ResponseEntity<RetornarPartidaDTO> partida;
        RestTemplate restTemplate = new RestTemplate();
        try {
            // Faz a requisição e captura a resposta
            partida = restTemplate.getForEntity(
                    "http://localhost:8080/partida/" + aposta.getIdPartida(),
                    RetornarPartidaDTO.class);
        } catch (Exception e) {
            // Lida com exceções que podem ocorrer durante a requisição
            throw new PartidaNaoEncontradaException("Erro ao buscar a partida: " + e.getMessage());
        }




        if (partida.getStatusCode().is2xxSuccessful())  {
            apostaRepository.save(aposta);
        } else if (partida.getStatusCode().is4xxClientError()) {
            throw new PartidaNaoEncontradaException("Partida não encontrada ou inválida!");
        }



    }

    public List<Aposta> listar(String status) {
        if (status != null) {
            return apostaRepository.findByStatus(status);
        }

        return apostaRepository.findAll();
    }

    public Aposta getAposta(String id) {
        Optional<Aposta> op = apostaRepository.findById(id);
        if (op.isPresent()) {
            Aposta aposta = op.get();

            if (aposta.getStatus().equals("REALIZADA")) {

                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<RetornarPartidaDTO> response = restTemplate.getForEntity(
                        "http://localhost:8080/partida/" + aposta.getIdPartida(),
                        RetornarPartidaDTO.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    RetornarPartidaDTO partida = response.getBody();

                    if (partida.getStatus().equals("REALIZADA")) {
                        if ((aposta.getResultado().equals("VITORIA_MANDANTE") && partida.getPlacarMandante() > partida.getPlacarVisitante())
                        || (aposta.getResultado().equals("VITORIA_VISITANTE") && partida.getPlacarMandante() < partida.getPlacarVisitante())
                        || (aposta.getResultado().equals("EMPATE") && partida.getPlacarMandante() == partida.getPlacarVisitante())) {

                            aposta.setStatus("GANHOU");
                        } else {
                            aposta.setStatus("PERDIDA");
                        }
                    }
                }

            }
            apostaRepository.save(aposta);
            return aposta;
        }
        throw new RuntimeException("Aposta " + id + " não encontrada");
    }

}
