package br.edu.ifpb.gugawag.so.sockets;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class NFSServer {
    private static List<String> arquivos = new ArrayList<>();

    public static void main(String[] args) {
        int portNumber = 12345;

        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            System.out.println("Servidor NFS iniciado na porta " + portNumber);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String command;
                while ((command = in.readLine()) != null) {
                    String[] parts = command.split(" ");
                    String operation = parts[0];

                    switch (operation) {
                        case "readdir":
                            listFiles(out);
                            break;
                        case "rename":
                            String oldName = parts[1];
                            String newName = parts[2];
                            renameFile(oldName, newName, out);
                            break;
                        case "create":
                            String fileName = parts[1];
                            createFile(fileName, out);
                            break;
                        case "remove":
                            String fileToRemove = parts[1];
                            removeFile(fileToRemove, out);
                            break;
                        default:
                            out.println("Comando não reconhecido");
                    }
                }

                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void listFiles(PrintWriter out) {
            for (String file : arquivos) {
                out.println(file);
            }
            out.println("FIM");
        }

        private void renameFile(String oldName, String newName, PrintWriter out) {
            if (arquivos.contains(oldName)) {
                arquivos.remove(oldName);
                arquivos.add(newName);
                out.println("Renomeado com sucesso.");
            } else {
                out.println("Arquivo não encontrado.");
            }
        }

        private void createFile(String fileName, PrintWriter out) {
            arquivos.add(fileName);
            out.println("Arquivo criado com sucesso.");
        }

        private void removeFile(String fileName, PrintWriter out) {
            if (arquivos.contains(fileName)) {
                arquivos.remove(fileName);
                out.println("Arquivo removido com sucesso.");
            } else {
                out.println("Arquivo não encontrado.");
            }
        }
    }
}