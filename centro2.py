from cancha import Cancha
from reservas import Reserva
from persona import Cliente, Empleado

class Centro:
    def __init__(self, nombre, direccion):
        self.nombre = nombre
        self.direccion = direccion
        self.canchas = []
        self.clientes = []
        self.reservas = []
        self.empleados = []

    def main_menu(self):
        """
        Menú principal del centro.
        """
        while True:
            print("\n--- Menú Principal ---")
            print("1. Gestionar Canchas")
            print("2. Gestionar Clientes")
            print("3. Gestionar Empleados")
            print("4. Gestionar Reservas")
            print("5. Salir")

            opcion = input("Seleccione una opción: ")

            if opcion == '1':
                self.submenu_canchas()
            elif opcion == '2':
                self.submenu_clientes()
            elif opcion == '3':
                self.submenu_empleados()
            elif opcion == '4':
                self.submenu_reservas()
            elif opcion == '5':
                break
            else:
                print("Opción inválida.")

    def submenu_canchas(self):
        """
        Submenú para gestionar las operaciones con canchas en el menú principal.
        """
        while True:
            print("\n--- Gestionar Canchas ---")
            print("a. Crear una cancha")
            print("b. Agregar una cancha al centro")
            print("c. Listar canchas para un deporte")
            print("d. Eliminar una cancha del centro")
            print("e. Volver al menú principal")

            opcion = input("Seleccione una opción: ")

            if opcion == 'a':
                cancha = Cancha()
                cancha.crear_cancha()

            elif opcion == 'b':
                if hasattr(cancha, 'numero_cancha'):
                    cancha.agregar_cancha(self)
                    print("Cancha agregada al centro.")
                else:
                    print("Debe crear una cancha primero.")

            elif opcion == 'c':
                deporte = input("Ingrese el deporte: ")
                Cancha.mostrar_canchas_para_deporte(deporte, self)

            elif opcion == 'd':
                numero_cancha = input("Ingrese el número de cancha a eliminar: ")
                cancha_a_eliminar = next((c for c in self.canchas if c.numero_cancha == numero_cancha), None)
                if cancha_a_eliminar:
                    cancha_a_eliminar.quitar_cancha(self)
                else:
                    print("Cancha no encontrada.")

            elif opcion == 'e':
                break

            else:
                print("Opción inválida.")

    def submenu_reservas(self):
        """
        Submenú para gestionar las operaciones con reservas en el menú principal.
        """
        while True:
            print("\n--- Gestionar Reservas ---")
            print("a. Crear una reserva")
            print("b. Listar reservas de una cancha")
            print("c. Listar reservas de un cliente")
            print("d. Listar reservas por nº de reserva")
            print("e. Volver al menú principal")

            opcion = input("Seleccione una opción: ")

            if opcion == 'a': # Crear una reserva
                if self.clientes and self.canchas:
                    numero_reserva = input("Ingrese el número de reserva: ")
                    fecha = input("Ingrese la fecha (YYYY-MM-DD): ")
                    cliente = self.seleccionar_cliente()
                    cancha = self.seleccionar_cancha()
                    if cancha and cliente:
                        reserva = Reserva(numero_reserva, fecha, cliente, cancha)
                        self.reservas.append(reserva)
                        print("Reserva creada exitosamente.")
                    else:
                        print("Cliente o cancha no válidos.")
                else:
                    print("Debe haber al menos un cliente y una cancha para crear una reserva.")

            elif opcion == 'b': # Listar reservas de una cancha
                numero_cancha = input("Ingrese el número de cancha: ")
                cancha = next((c for c in self.canchas if c.numero_cancha == numero_cancha), None)
                if cancha:
                    reservas_cancha = [reserva for reserva in self.reservas if reserva.cancha == cancha]
                    if reservas_cancha:
                        print(f"Reservas para la cancha {numero_cancha}:")
                        for reserva in reservas_cancha:
                            print(f"Número de reserva: {reserva.numero_reserva}, Fecha: {reserva.fecha}, Cliente: {reserva.cliente.nombre}")
                    else:
                        print("No hay reservas para esta cancha.")
                else:
                    print("Cancha no encontrada.")

            elif opcion == 'c': # Listar reservas de un cliente
                cliente = self.seleccionar_cliente()
                if cliente:
                    reservas_cliente = [reserva for reserva in self.reservas if reserva.cliente == cliente]
                    if reservas_cliente:
                        print(f"Reservas para el cliente {cliente.nombre}:")
                        for reserva in reservas_cliente:
                            print(f"Número de reserva: {reserva.numero_reserva}, Fecha: {reserva.fecha}, Cancha: {reserva.cancha.numero_cancha}")
                    else:
                        print("No hay reservas para este cliente.")
                else:
                    print("Cliente no encontrado.")

            elif opcion == 'd': # Listar reservas por nº de reserva
                numero_reserva = input("Ingrese el número de reserva: ")
                reserva = next((reserva for reserva in self.reservas if reserva.numero_reserva == numero_reserva), None)
                if reserva:
                    print(f"Reserva encontrada: {reserva}")
                else:
                    print("Reserva no encontrada.")

            elif opcion == 'e': # Volver al menú principal
                break

            else:
                print("Opción inválida.")

    def submenu_clientes(self):
        """
        Submenú para gestionar las operaciones con clientes en el menú principal.
        """
        while True:
            print("\n--- Gestionar Clientes ---")
            print("a. Crear un cliente")
            print("b. Agregar un cliente al centro")
            print("c. Quitar un cliente del centro")
            print("d. Listar clientes morosos")
            print("e. Volver al menú principal")

            opcion = input("Seleccione una opción: ")

            if opcion == 'a': # Crear un cliente
                cliente = Cliente.crear_cliente()
                if cliente:
                    self.clientes.append(cliente)


centro_deportivo = Centro("Centro Deportivo XYZ", "Calle Principal 123")
centro_deportivo.main_menu()
