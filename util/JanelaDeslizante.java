/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 14 11 2025
* Ultima alteracao.: 14 11 2025
* Nome.............: JanelaDeslizante
* Funcao...........: Estrutura de dados para a janela deslizante
*************************************************************** */

package util;

public class JanelaDeslizante {

  public final String JANELA_TRANSMISSORA = "transmissor";
  public final String JANELA_RECEPTOR = "recepetor";

  // Espaco de sequencia total (0 a 7)
  private final int MAX_SEQUENCIA = 8;

  // O buffer para guardar os quadros enviados
  private int[][] bufferDeQuadros;

  private int tamanhoJanela; // Quantos quadros podem ser enviados na rede
  private int base; // Limite inferior da janela
  private int proximoSeqNum; // Limite superior do janela

  // Identifica se a janela sera usada na transmissao ou na recepcao
  private String tipoDeJanela;

  public JanelaDeslizante(int tamanhoJanela, String tipoDeJanela) {
    // Verifica que a janela e maior que o buffer
    if (tamanhoJanela >= MAX_SEQUENCIA) {
      throw new IllegalArgumentException("Janela deve ser menor que o espaco de sequencia (max 7).");
    } // fim do if

    this.tamanhoJanela = tamanhoJanela;
    this.tipoDeJanela = tipoDeJanela;
    base = 0;
    bufferDeQuadros = new int[tamanhoJanela][];
    // Caso a janela seja utilizada para transmissao...
    if (tipoDeJanela == JANELA_TRANSMISSORA) {
      // Ela inicializa com tudo 0
      proximoSeqNum = 0;
    } else if (tipoDeJanela == JANELA_RECEPTOR) {
      // Senao ela inicializa com o intervalo dos quadros a serem recebidos
      proximoSeqNum = tamanhoJanela;
    } // fim do if
  }
  
  public void receberAck(int quadro[]) {
    int numDeSequencia = quadro[0] >> 24;
  }
}
