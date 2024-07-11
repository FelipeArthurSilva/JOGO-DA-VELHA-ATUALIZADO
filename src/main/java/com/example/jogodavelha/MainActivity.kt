package com.example.jogodavelha

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.jogodavelha.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    // Lateinit para inicializar a vinculação da view mais tarde
    private lateinit var binding: ActivityMainBinding

    // Constantes para os jogadores
    private val JOGADOR_X = "X"
    private val JOGADOR_O = "O"

    // Array 2D para representar o tabuleiro
    private val tabuleiro = Array(3) { Array(3) { "" } }
    // Variável para controlar o jogador atual
    private var jogadorAtual = JOGADOR_X

    // Handler para lidar com atrasos na UI thread
    private val handler = Handler(Looper.getMainLooper())

    // Variável para armazenar o nível de dificuldade
    private var nivelDificuldade = "facil"

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Configuração dos cliques nos botões
        setupButtons()

        // Exemplo de como mudar o nível de dificuldade
        nivelDificuldade = "dificil" // Pode ser "facil" ou "dificil"
    }

    // Configura os botões e reseta o tabuleiro
    private fun setupButtons() {
        val buttons = listOf(
            binding.buttonZero, binding.buttonUm, binding.buttonDois,
            binding.buttonTres, binding.buttonQuatro, binding.buttonCinco,
            binding.buttonSeis, binding.buttonSete, binding.buttonOito
        )
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener { buttonClick(it, index) }
        }
        resetBoard()
    }

    // Reseta o tabuleiro e os botões
    private fun resetBoard() {
        val buttons = listOf(
            binding.buttonZero, binding.buttonUm, binding.buttonDois,
            binding.buttonTres, binding.buttonQuatro, binding.buttonCinco,
            binding.buttonSeis, binding.buttonSete, binding.buttonOito
        )
        buttons.forEach { button ->
            button.text = ""
            button.setBackgroundResource(android.R.drawable.btn_default)
            button.isEnabled = true
        }
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                tabuleiro[i][j] = ""
            }
        }
        jogadorAtual = JOGADOR_X
    }

    // Handler do clique dos botões
    private fun buttonClick(view: View, index: Int) {
        val buttonSelecionado = view as Button
        buttonSelecionado.text = jogadorAtual
        buttonSelecionado.isEnabled = false

        // Calcula a linha e coluna do botão clicado
        val row = index / 3
        val col = index % 3
        tabuleiro[row][col] = jogadorAtual

        // Muda a aparência do botão com base no jogador atual
        if (jogadorAtual == JOGADOR_X) {
            buttonSelecionado.setBackgroundResource(R.drawable.simbolofla)
        } else {
            buttonSelecionado.setBackgroundResource(R.drawable.flu)
        }

        // Verifica se há um vencedor
        val vencedor = verificaVencedor(tabuleiro)
        if (vencedor != null) {
            Toast.makeText(this, "Vencedor: $vencedor", Toast.LENGTH_LONG).show()
            resetBoard()
            return
        }

        // Verifica se há empate
        if (verificaEmpate()) {
            Toast.makeText(this, "Empate", Toast.LENGTH_LONG).show()
            resetBoard()
            return
        }

        // Alterna o jogador atual
        jogadorAtual = if (jogadorAtual == JOGADOR_X) JOGADOR_O else JOGADOR_X
        // Se for a vez da máquina jogar
        if (jogadorAtual == JOGADOR_O) {
            desabilitarBotoes()
            handler.postDelayed({
                maquinaJogar()
                habilitarBotoes()
            }, 1000) // Adiciona um delay de 1 segundo
        }
    }

    // Função para a máquina fazer uma jogada
    private fun maquinaJogar() {
        if (nivelDificuldade == "facil") {
            jogadaFacil()
        } else {
            jogadaDificil()
        }
    }

    // Jogada fácil, a máquina escolhe uma posição aleatória vazia
    private fun jogadaFacil() {
        val posicoesVazias = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (tabuleiro[i][j].isEmpty()) {
                    posicoesVazias.add(Pair(i, j))
                }
            }
        }
        if (posicoesVazias.isNotEmpty()) {
            val (linha, coluna) = posicoesVazias[Random.nextInt(posicoesVazias.size)]
            val button = getButton(linha, coluna)
            button.performClick()
        }
    }

    // Jogada difícil, a máquina tenta vencer ou bloquear o jogador
    private fun jogadaDificil() {
        val melhorJogada = melhorJogada(JOGADOR_O) ?: melhorJogada(JOGADOR_X) ?: jogadaAleatoria()
        val (linha, coluna) = melhorJogada
        val button = getButton(linha, coluna)
        button.text = JOGADOR_O
        button.setBackgroundResource(R.drawable.flu)
        button.isEnabled = false
        tabuleiro[linha][coluna] = JOGADOR_O

        // Verifica se há um vencedor ou empate após a jogada da máquina
        val vencedor = verificaVencedor(tabuleiro)
        if (vencedor != null) {
            handler.postDelayed({
                Toast.makeText(this, "Vencedor: $vencedor", Toast.LENGTH_LONG).show()
                resetBoard()
            }, 500)
        } else if (verificaEmpate()) {
            handler.postDelayed({
                Toast.makeText(this, "Empate", Toast.LENGTH_LONG).show()
                resetBoard()
            }, 500)
        } else {
            jogadorAtual = JOGADOR_X
        }
    }

    // Função para encontrar a melhor jogada
    private fun melhorJogada(jogador: String): Pair<Int, Int>? {
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (tabuleiro[i][j].isEmpty()) {
                    tabuleiro[i][j] = jogador
                    if (verificaVencedor(tabuleiro) == jogador) {
                        tabuleiro[i][j] = ""
                        return Pair(i, j)
                    }
                    tabuleiro[i][j] = ""
                }
            }
        }
        return null
    }

    // Função para escolher uma jogada aleatória
    private fun jogadaAleatoria(): Pair<Int, Int> {
        val posicoesVazias = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (tabuleiro[i][j].isEmpty()) {
                    posicoesVazias.add(Pair(i, j))
                }
            }
        }
        return posicoesVazias[Random.nextInt(posicoesVazias.size)]
    }

    // Função para obter um botão específico com base na linha e coluna
    private fun getButton(row: Int, col: Int): Button {
        return when (row * 3 + col) {
            0 -> binding.buttonZero
            1 -> binding.buttonUm
            2 -> binding.buttonDois
            3 -> binding.buttonTres
            4 -> binding.buttonQuatro
            5 -> binding.buttonCinco
            6 -> binding.buttonSeis
            7 -> binding.buttonSete
            8 -> binding.buttonOito
            else -> throw IllegalArgumentException("Posição inválida")
        }
    }

    // Função para verificar se há um vencedor
    private fun verificaVencedor(tabuleiro: Array<Array<String>>): String? {
        for (i in 0 until 3) {
            if (tabuleiro[i][0] == tabuleiro[i][1] && tabuleiro[i][1] == tabuleiro[i][2] && tabuleiro[i][0].isNotEmpty()) {
                return tabuleiro[i][0]
            }
            if (tabuleiro[0][i] == tabuleiro[1][i] && tabuleiro[1][i] == tabuleiro[2][i] && tabuleiro[0][i].isNotEmpty()) {
                return tabuleiro[0][i]
            }
        }
        if (tabuleiro[0][0] == tabuleiro[1][1] && tabuleiro[1][1] == tabuleiro[2][2] && tabuleiro[0][0].isNotEmpty()) {
            return tabuleiro[0][0]
        }
        if (tabuleiro[0][2] == tabuleiro[1][1] && tabuleiro[1][1] == tabuleiro[2][0] && tabuleiro[0][2].isNotEmpty()) {
            return tabuleiro[0][2]
        }
        return null
    }

    // Função para verificar se há empate
    private fun verificaEmpate(): Boolean {
        return tabuleiro.all { row -> row.all { it.isNotEmpty() } }
    }

    // Desabilita todos os botões
    private fun desabilitarBotoes() {
        val buttons = listOf(
            binding.buttonZero, binding.buttonUm, binding.buttonDois,
            binding.buttonTres, binding.buttonQuatro, binding.buttonCinco,
            binding.buttonSeis, binding.buttonSete, binding.buttonOito
        )
        buttons.forEach { it.isEnabled = false }
    }

    // Habilita todos os botões
    private fun habilitarBotoes() {
        val buttons = listOf(
            binding.buttonZero, binding.buttonUm, binding.buttonDois,
            binding.buttonTres, binding.buttonQuatro, binding.buttonCinco,
            binding.buttonSeis, binding.buttonSete, binding.buttonOito
        )
        buttons.forEach { it.isEnabled = true }
    }
}
