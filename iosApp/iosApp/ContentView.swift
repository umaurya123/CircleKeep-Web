import UIKit
import SwiftUI
import ComposeApp

@MainActor
struct ComposeView: UIViewControllerRepresentable {
    @Binding var importData: String?

    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            importData: importData,
            onImportConsumed: {
                self.importData = nil
            }
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {
        // If external data arrives while app is running
        if let data = importData {
            MainViewControllerKt.handleExternalImport(data: data)
            DispatchQueue.main.async {
                self.importData = nil
            }
        }
    }
}

struct ContentView: View {
    @Binding var importData: String?

    var body: some View {
        ComposeView(importData: $importData)
            .ignoresSafeArea()
    }
}
