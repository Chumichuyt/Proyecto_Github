"""
    Módulo central centro1.py 
    Implementado por: Alvaro Fernandez Becerra ( Alumno1 )
    
"""
from cancha import Cancha
from reservas import Reserva
from persona import Cliente, Empleado
import datetime

class Centro:
    def __init__(self, nombre, direccion):
        self.nombre = nombre
        self.direccion = direccion
        self.canchas = []
        self.clientes = []
        self.reservas = []

    def main_menu(self):
        while True:
            print("\n--- Menú Principal ---")
            print("1. Gestionar Canchas")
            print("2. Gestionar Clientes")
            print("3. Gestionar Empleados")
            print("4. Gestionar Reservas")
            print("5. Salir")

            opcion = input("Seleccione una opción: ")

            if opcion == '1':
                self.gestionar_canchas()
            elif opcion == '2':
                self.gestionar_clientes()
            elif opcion == '3':
                self.gestionar_empleados()
            elif opcion == '4':
                self.gestionar_reservas()
            elif opcion == '5':
                break
            else:
                print("Opción inválida.")

    def submenu_canchas(self):
        """
            Sumbmenu para gestionar las operaciones con canchas en el menú principal
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
                if hasattr(cancha, 'numero_cancha'):  # Verificar si se creó una cancha
                    cancha.agregar_cancha(self)
                    print("Cancha agregada al centro.")
                else:
                    print("Debe crear una cancha primero.")
                    
            elif opcion == 'c':
                deporte = input("Ingrese el deporte: ")
                Cancha.mostrar_canchas_para_deporte(deporte, self)
                
            elif opcion == 'd':
                
                numero_cancha = input("Ingrese el número de cancha a eliminar: ")
                cancha_a_eliminar = None
                
                for cancha in self.canchas:
                    if cancha.numero_cancha == numero_cancha:
                        cancha_a_eliminar = cancha
                        break
                    
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
            Sumbmenu para gestionar las operaciones con reservas en el menú principal
        """
        while True:
            print("\n--- Gestionar Reservas ---")
            print("a. Crear una reserva")
            print("b. Listar reservas de una cancha")
            print("c. Listar reservas de un cliente")
            print("d. Listar reservas por nº de reserva")
            print("e. Registrar pago de un cliente")
            print("f. Mostrar saldo de un cliente")
            print("g. Volver al menú principal")

            opcion = input("Seleccione una opción: ")

            if opcion == 'a': #Crear reserva
                if self.clientes and self.canchas:  # Verificar si hay clientes y canchas
                    numero_reserva = input("Ingrese el número de reserva: ")
                    fecha_str = input("Ingrese la fecha (YYYY-MM-DD): ")
                    try:
                        fecha = datetime.datetime.strptime(fecha_str, "%Y-%m-%d").date()
                    except ValueError:
                        print("Formato de fecha incorrecto.")
                        continue

                    # Obtener cliente:
                    print("Clientes disponibles:")
                    for i, cliente in enumerate(self.clientes, start=1):
                        print(f"{i}. {cliente.nombre}") 
                    cliente_index = int(input("Seleccione el número de cliente: ")) - 1
                    cliente = self.clientes[cliente_index] if 0 <= cliente_index < len(self.clientes) else None

                    #Obtener cancha:
                    deporte = input("Qué deporte quiere practicar?:")
                    Cancha.mostrar_canchas_para_deporte(deporte,self)
                    
                    numero_cancha = input("Seleccione el número de cancha: ")
                    cancha = next((c for c in self.canchas if c.numero_cancha == numero_cancha), None)

                    #Solo si hay una cancha y un cliente se realiza la reserva
                    if cancha and cliente:
                        reserva = Reserva.crear_reserva(numero_reserva, fecha, cliente, cancha, self.reservas)
                        if reserva:
                            self.reservas.append(reserva)
                    else:
                        print("Cliente o cancha no válidos.")
                else:
                    print("Debe haber al menos un cliente y una cancha para crear una reserva.")

            elif opcion == 'b': # Listar reservas de una cancha
                deporte = input("Qué deporte se practica en la cancha que quiere consultar?:")
                Cancha.mostrar_canchas_para_deporte(deporte,self)
                
                numero_cancha = input("Seleccione el número de cancha: ")
                cancha = next((c for c in self.canchas if c.numero_cancha == numero_cancha), None)
                if cancha:
                    Reserva.listar_reservas_cancha(cancha, self.reservas)
                else:
                    print("Cancha no encontrada.")

            elif opcion == 'c': # Listar reservas de un cliente
                identificador_cliente = input("Ingrese el identificador del cliente: ")
                cliente = next((c for c in self.clientes if c.identificador == identificador_cliente), None)
                if cliente:
                    Reserva.listar_reservas_cliente(cliente, self.reservas)
                else:
                    print("Cliente no encontrado.")

            elif opcion == 'd': # Listar reservas por nº de reserva
                numero_reserva = input("Ingrese el número de reserva: ")
                reserva_encontrada = False
                
                for reserva in self.reservas:
                    if reserva.numero_reserva == numero_reserva:
                        print(reserva)  
                        reserva_encontrada = True
                        break
                if not reserva_encontrada:
                    print("Reserva no encontrada.")

            elif opcion == 'e': # Registrar pago de un cliente
                identificador_cliente = input("Ingrese el identificador del cliente: ")
                cliente = next((c for c in self.clientes if c.identificador == identificador_cliente), None)
                
                if cliente:
                    monto = float(input("Ingrese el monto del pago: "))
                    cliente.registrar_pago(monto)  # Método a implementar en la clase Cliente
                else:
                    print("Cliente no encontrado.")

            elif opcion == 'f': # Mostrar saldo de un cliente
                identificador_cliente = input("Ingrese el identificador del cliente: ")
                cliente = next((c for c in self.clientes if c.identificador == identificador_cliente), None)
                
                if cliente:
                    cliente.mostrar_saldo()  # Método a implementar en la clase Cliente
                else:
                    print("Cliente no encontrado.")

            elif opcion == 'g':
                break
            
            else:
                print("Opción inválida.")

    def submenu_clientes(self):
        """
            Sumbmenu para gestionar las operaciones con clientes en el menú principal
        """
        while True:
            print("\n--- Gestionar Clientes ---")
            print("a. Crear cliente")
            print("b. Agregar cliente al centro")
            print("c. Eliminar un cliente del centro")
            print("d. Listar clientes morosos (Alumno 2)")  # Implementado por el alumno 2
            print("e. Volver al menú principal")

            opcion = input("Seleccione una opción: ")

            if opcion == 'a':
                cliente = Cliente.crear_cliente()
            elif opcion == 'b':
                if hasattr(cliente, 'identificador'):  # Verificar si se creó un cliente
                    cliente.agregar_cliente(self)
                else:
                    print("Debe crear un cliente primero.")
            elif opcion == 'c':
                identificador_cliente = input("Ingrese el identificador del cliente a eliminar: ")
                cliente_a_eliminar = None
                for cliente in self.clientes:
                    if cliente.identificador == identificador_cliente:
                        cliente_a_eliminar = cliente
                        break
                if cliente_a_eliminar:
                    cliente_a_eliminar.quitar_cliente(self)
                else:
                    print("Cliente no encontrado.")
            elif opcion == 'd':
                # FUNCIÓN QUE IMPLEMENTA EL ALUMNO2 (MANU)
                # LISTAR CLIENTES MOROSOS
                print("Funcionalidad a implementar por Alumno2.")
            elif opcion == 'e':
                break
            else:
                print("Opción inválida.")

    def submenu_empleados(self):
        """
            Sumbmenu para gestionar las operaciones con empleados en el menú principal
        """
        while True:
            print("\n--- Gestionar Empleados ---")
            print("a. Crear un empleado")
            print("b. Registrar un empleado a una cancha")
            print("c. Asignar tarea a un empleado")
            print("d. Quitar tarea de un empleado")
            print("e. Listar empleados desocupados")
            print("f. Quitar un empleado de la cancha")
            print("g. Volver al menú principal")

            opcion = input("Seleccione una opcion: ")

            if opcion == 'a': # Crear un empleado
                empleado = Empleado.crear_empleado()
                self.empleados.append(empleado)
                print("Empleado creado exitosamente.")

            elif opcion == 'b':# Registrar un empleado a una cancha
                if self.empleados and self.canchas:
                    Empleado.listar_empleados_desocupados(self)
                    nombre_empleado = input("Ingrese el nombre del empleado: ")
                    apellido_empleado = input("Ingrese el apellido del empleado: ")
                    empleado = next((e for e in self.empleados if e.nombre == nombre_empleado and e.apellido == apellido_empleado), None)
                    if empleado:
                        Cancha.mostrar_canchas_para_deporte(self)
                        numero_cancha = input("Ingrese el número de cancha: ")
                        cancha = next((c for c in self.canchas if c.numero_cancha == numero_cancha), None)
                        if cancha:
                            empleado.registrar_en_cancha(cancha)
                        else:
                            print("Cancha no encontrada.")
                    else:
                        print("Empleado no encontrado / no está desocupado.")
                else:
                    print("Debe haber al menos un empleado desocupado y una cancha para registrarlo.")

            elif opcion == 'c':# Asignar tarea a un empleado
                nombre_empleado = input("Ingrese el nombre del empleado: ")
                apellido_empleado = input("Ingrese el apellido del empleado: ")
                empleado = next((emp for emp in self.empleados if emp.nombre == nombre_empleado and emp.apellido == apellido_empleado), None)
                
                if empleado:
                    tarea = input("Ingrese la tarea a asignar: ")
                    empleado.asignar_tarea(tarea)
                else:
                    print("Empleado no encontrado.")

            elif opcion == 'd': # Quitar tarea de un empleado
                nombre_empleado = input("Ingrese el nombre del empleado: ")
                apellido_empleado = input("Ingrese el apellido del empleado: ")
                empleado = next((emp for emp in self.empleados if emp.nombre == nombre_empleado and emp.apellido == apellido_empleado), None)
                
                if empleado:
                    tarea = input("Ingrese la tarea a quitar: ")
                    empleado.quitar_tarea(tarea)
                else:
                    print("Empleado no encontrado.")

            elif opcion == 'e':# Listar empleados desocupados
                Empleado.listar_empleados_desocupados(self)

            elif opcion == 'f':# Quitar un empleado de la cancha
                nombre_empleado = input("Ingrese el nombre del empleado: ")
                apellido_empleado = input("Ingrese el apellido del empleado: ")
                empleado = next((emp for emp in self.empleados if emp.nombre == nombre_empleado and emp.apellido == apellido_empleado), None)
                
                if empleado:
                    if empleado.cancha_asignada:
                        empleado.quitar_de_cancha(empleado.cancha_asignada)
                    else:
                        print("El empleado no está asignado a ninguna cancha.")
                else:
                    print("Empleado no encontrado.")

            elif opcion == 'g':
                break
            else:
                print("Opción inválida.")


#Ejecución del programa
centro = Centro("Centro Deportivo", "Calle Principal 123")
centro.main_menu()
