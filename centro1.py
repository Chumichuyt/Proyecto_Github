"""
    Módulo central centro1.py 
    Implementado por: Alvaro Fernandez Becerra ( Alumno1 )
    
"""
from cancha import Cancha
from reservas import Reserva
#from persona import Persona
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
                # Pendiente de implementar con módulo persona.py
                print("Funcionalidad no disponible aún. (Alumno 2)")

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
                # Pendiente de implementar con módulo persona.py
                print("Funcionalidad no disponible aún. (persona.py)")

            elif opcion == 'f': # Mostrar saldo de un cliente
                # Pendiente de implementar con módulo persona.py
                print("Funcionalidad no disponible aún. (persona.py)")

            elif opcion == 'g':
                break
            
            else:
                print("Opción inválida.")

    def submenu_clientes(self):
        """
            Sumbmenu para gestionar las operaciones con clientes en el menú principal
        """
        # Pendiente de implementar con módulo persona.py
        pass

    def submenu_empleados(self):
        """
            Sumbmenu para gestionar las operaciones con empleados en el menú principal
        """
        # Pendiente de implementar con módulo persona.py
        pass

    

#Ejecución del programa
centro = Centro("Centro Deportivo", "Calle Principal 123")
centro.main_menu()
