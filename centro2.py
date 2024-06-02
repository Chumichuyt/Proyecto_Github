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
                pass
                print("funcion que implementa Alvaro")

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

            elif opcion == 'b': # Agregar un cliente al centro
                cliente = self.seleccionar_cliente()
                if cliente:
                    self.clientes.append(cliente)
                    print(f"Cliente {cliente.nombre} agregado al centro.")

            elif opcion == 'c': # Quitar un cliente del centro
                cliente = self.seleccionar_cliente()
                if cliente in self.clientes:
                    self.clientes.remove(cliente)
                    print(f"Cliente {cliente.nombre} removido del centro.")

            elif opcion == 'd': # Listar clientes morosos
                clientes_morosos = [cliente for cliente in self.clientes if cliente.saldo < 0]
                if clientes_morosos:
                    print("Clientes morosos:")
                    for cliente in clientes_morosos:
                        print(f"Nombre: {cliente.nombre}, Saldo: {cliente.saldo}")
                else:
                    print("No hay clientes morosos.")

            elif opcion == 'e': # Volver al menú principal
                break

            else:
                print("Opción inválida.")

    def submenu_empleados(self):
        """
        Submenú para gestionar las operaciones con empleados en el menú principal.
        """
        while True:
            print("\n--- Gestionar Empleados ---")
            print("a. Crear un empleado")
            print("b. Agregar un empleado al centro")
            print("c. Quitar un empleado del centro")
            print("d. Listar todos los empleados")
            print("e. Volver al menú principal")

            opcion = input("Seleccione una opción: ")

            if opcion == 'a': # Crear un empleado
                empleado = Empleado.crear_empleado()
                if empleado:
                    self.empleados.append(empleado)

            elif opcion == 'b': # Agregar un empleado al centro
                empleado = self.seleccionar_empleado()
                if empleado:
                    self.empleados.append(empleado)
                    print(f"Empleado {empleado.nombre} agregado al centro.")

            elif opcion == 'c': # Quitar un empleado del centro
                empleado = self.seleccionar_empleado()
                if empleado in self.empleados:
                    self.empleados.remove(empleado)
                    print(f"Empleado {empleado.nombre} removido del centro.")

            elif opcion == 'd': # Listar todos los empleados
                if self.empleados:
                    print("Empleados del centro:")
                    for empleado in self.empleados:
                        print(f"Nombre: {empleado.nombre}, ID: {empleado.id}")
                else:
                    print("No hay empleados en el centro.")

            elif opcion == 'e': # Volver al menú principal
                break

            else:
                print("Opción inválida.")

    def seleccionar_cliente(self):
        """
        Selecciona un cliente de la lista de clientes.
        """
        if not self.clientes:
            print("No hay clientes disponibles.")
            return None
        print("Clientes disponibles:")
        for i, cliente in enumerate(self.clientes, start=1):
            print(f"{i}. {cliente}")
        while True:
            try:
                seleccion = int(input("Seleccione un cliente (0 para cancelar): "))
                if seleccion == 0:
                    return None
                elif 1 <= seleccion <= len(self.clientes):
                    return self.clientes[seleccion - 1]
                else:
                    print("Opción inválida. Intente de nuevo.")
            except ValueError:
                print("Opción inválida. Intente de nuevo.")


centro_deportivo = Centro("Centro Deportivo XYZ", "Calle Principal 123")
centro_deportivo.main_menu()
