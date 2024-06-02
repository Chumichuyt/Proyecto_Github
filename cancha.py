class Cancha:
    def __init__(self, numero_cancha = None, deporte= None, precio = None, habilitada = None):
        self.numero_cancha = numero_cancha
        self.deporte = deporte
        self.precio = precio
        self.habilitada = habilitada
        self.lista_reservas = []
        self.lista_empleados = []

    def crear_cancha(self):
        """
            Método que crea una cancha con los datos introducidos por el usuario
        """
        
        try:
            self.numero_cancha = input("Ingresa el numero de la cancha: ")
            self.deporte = input("Qué deporte se va a jugar en la cancha: ")
            self.precio = float(input("Qué precio va a tener la cancha: "))
            self.habilitada = input("¿La cancha va a estar habilitada para jugar? (Si/No): ").lower()
            
        except ValueError as Err:
            print("Error" + Err)

    def agregar_cancha(self, centro):
            """
            Método que agrega una cancha a la lista de canchas del centro.
            - self: instancia de la clase Cancha.
            - centro: instancia de la clase Centro.
            """
            if self not in centro.canchas:
                centro.canchas.append(self)
                print("Cancha agregada al centro exitosamente.")
            else:
                print("La cancha ya está registrada en el centro.")

    def mostrar_deportes_disponibles(self, centro):
        """
        Método que muestra los deportes disponibles para jugar en la cancha.
        - centro: Instancia de la clase Centro.
        """
        deportes_disponibles = []
        print("Deportes disponibles:")
        for cancha in centro.canchas:
            if cancha.deporte not in deportes_disponibles:
                deportes_disponibles.append(cancha.deporte)
        
        for deporte in deportes_disponibles:
            print(deporte)

    @staticmethod
    def mostrar_canchas_para_deporte(deporte, centro):
        """
        Método que muestra las canchas disponibles para un deporte en particular.
        - deporte: Deporte de las canchas que se van a mostrar.
        - centro: Instancia de la clase Centro.
        """
        
        print(f"Canchas disponibles para {deporte}:")
        for i, cancha in enumerate(centro.canchas, start=1):
            if cancha.deporte == deporte:
                print(f"{i}. Número de cancha: {cancha.numero_cancha}")

    def quitar_cancha(self, centro):
        """
        Método que elimina una cancha de la lista de canchas del centro.
        - self: instancia de la clase Cancha.
        - centro: instancia de la clase Centro.
        """
        if self in centro.canchas:
            if not self.lista_reservas:
                centro.canchas.remove(self)
                print("Cancha eliminada del centro exitosamente.")
            else:
                print("La cancha no se puede eliminar porque tiene reservas pendientes.")
        else:
            print("La cancha no está registrada en el centro.")

    def __str__(self):
        return f"Número de cancha: {self.numero_cancha}, deporte: {self.deporte}, precio: {self.precio}, Disponible: {self.habilitada}"

